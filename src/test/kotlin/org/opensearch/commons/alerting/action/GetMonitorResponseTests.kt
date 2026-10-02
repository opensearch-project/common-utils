/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.Version
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.action.GetMonitorResponse.AssociatedWorkflow
import org.opensearch.commons.alerting.builder
import org.opensearch.commons.alerting.model.CronSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.parser
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.util.IndexUtils.Companion.INCLUDE_BACKEND_ROLES_PARAM
import org.opensearch.commons.alerting.util.string
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.xcontent.ToXContent
import java.time.Instant
import java.time.ZoneId

class GetMonitorResponseTests {

    private val olderVersion = Version.V_3_9_0
    private val includeBackendRoles = ToXContent.MapParams(mapOf(INCLUDE_BACKEND_ROLES_PARAM to "true"))

    @Test
    fun `test get monitor response`() {
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, null, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetMonitorResponse(sin)
        assertEquals("1234", newReq.id)
        assertEquals(1L, newReq.version)
        assertNull(newReq.monitor)
    }

    @Test
    fun `test get monitor response with monitor`() {
        val monitor = randomMonitor()
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, monitor, null)
        assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetMonitorResponse(sin)
        assertEquals("1234", newReq.id)
        assertEquals(1L, newReq.version)
        assertNotNull(newReq.monitor)
        assertNull(newReq.visibleBackendRoles)
    }

    @Test
    fun `test get monitor response with visible backend roles`() {
        val monitor = randomMonitor()
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, monitor, null, listOf("role-1", "role-2"))

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetMonitorResponse(sin)
        assertNotNull(newReq.monitor)
        assertEquals(listOf("role-1", "role-2"), newReq.visibleBackendRoles)
    }

    @Test
    fun `test toXContent writes only the visible backend roles of the monitor user`() {
        val monitor = randomMonitor()
        val user = monitor.user!!
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, monitor, null, listOf(user.backendRoles[0]))

        val xContentString = req.toXContent(builder(), includeBackendRoles).string()
        assertTrue(xContentString.contains("\"user\":{\"backend_roles\":[\"${user.backendRoles[0]}\"]}"))
        // The rest of the user object, and the backend role the requester does not belong to, stay hidden.
        assertFalse(xContentString.contains(user.name))
        assertFalse(xContentString.contains(user.backendRoles[1]))
        assertFalse(xContentString.contains("custom_attribute_names"))
    }

    @Test
    fun `test toXContent omits the user when no visible backend roles are resolved`() {
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), null)

        val xContentString = req.toXContent(builder(), includeBackendRoles).string()
        assertFalse(xContentString.contains("\"user\""))
    }

    @Test
    fun `test toXContent omits the user unless the caller asks for backend roles`() {
        val monitor = randomMonitor()
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, monitor, null, monitor.user!!.backendRoles)

        // Default parameters: the response is exactly what it was before backend roles could be exposed.
        val xContentString = req.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        assertFalse(xContentString.contains("\"user\""))
    }

    @Test
    fun `test associated workflows round trip`() {
        val workflows = listOf(AssociatedWorkflow("w1", "workflow-one"), AssociatedWorkflow("w2", "workflow-two"))
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), workflows)

        val newReq = roundTrip(req, Version.CURRENT)
        assertEquals(2, newReq.associatedWorkflows!!.size)
        assertEquals("w1", newReq.associatedWorkflows!![0].id)
        assertEquals("workflow-two", newReq.associatedWorkflows!![1].name)
    }

    @Test
    fun `test visible backend roles are not written to an older node`() {
        val withRoles = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), null, listOf("role-1", "role-2"))
        val withoutRoles = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), null)

        // An older node negotiates an older stream version, so the field is absent from the bytes entirely.
        assertEquals(bytesWritten(withoutRoles, olderVersion), bytesWritten(withRoles, olderVersion))
        // On a current stream it is present, so the two are no longer the same size.
        assertTrue(bytesWritten(withRoles, Version.CURRENT) > bytesWritten(withoutRoles, Version.CURRENT))
    }

    @Test
    fun `test response written for an older node reads back without the new field`() {
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), null, listOf("role-1"))

        val newReq = roundTrip(req, olderVersion)
        assertEquals("1234", newReq.id)
        assertNotNull(newReq.monitor)
        assertNull(newReq.visibleBackendRoles)
    }

    @Test
    fun `test toXContent omits the user when there are no roles to show`() {
        val req = GetMonitorResponse("1234", 1L, 2L, 0L, randomMonitor(), null, emptyList())

        val xContentString = req.toXContent(builder(), includeBackendRoles).string()
        assertFalse(xContentString.contains("\"user\""))
    }

    @Test
    fun `test a monitor carrying only backend roles can be parsed back by a client`() {
        val monitor = randomMonitor()
        val monitorJson = monitor.toXContentWithBackendRoles(builder(), ToXContent.EMPTY_PARAMS, listOf("role-1")).string()

        val parsed = Monitor.parse(parser(monitorJson))
        assertEquals(listOf("role-1"), parsed.user!!.backendRoles)
        // The owner's identity was never written, so it comes back empty rather than wrong.
        assertEquals("", parsed.user!!.name)
        assertTrue(parsed.user!!.roles.isEmpty())
    }

    private fun roundTrip(response: GetMonitorResponse, version: Version): GetMonitorResponse {
        val out = BytesStreamOutput()
        out.version = version
        response.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        sin.version = version
        return GetMonitorResponse(sin)
    }

    private fun bytesWritten(response: GetMonitorResponse, version: Version): Int {
        val out = BytesStreamOutput()
        out.version = version
        response.writeTo(out)
        return out.bytes().length()
    }

    private fun randomMonitor(): Monitor {
        val cronExpression = "31 * * * *" // Run at minute 31.
        val testInstance = Instant.ofEpochSecond(1538164858L)

        val cronSchedule = CronSchedule(cronExpression, ZoneId.of("Asia/Kolkata"), testInstance)
        return Monitor(
            id = "123",
            version = 0L,
            name = "test-monitor",
            enabled = true,
            schedule = cronSchedule,
            lastUpdateTime = Instant.now(),
            enabledTime = Instant.now(),
            monitorType = Monitor.MonitorType.QUERY_LEVEL_MONITOR.value,
            user = randomUser(),
            schemaVersion = 0,
            inputs = mutableListOf(),
            triggers = mutableListOf(),
            uiMetadata = mutableMapOf()
        )
    }
}
