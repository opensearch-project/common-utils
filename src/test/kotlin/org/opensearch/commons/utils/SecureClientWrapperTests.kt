/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.opensearch.action.ActionType
import org.opensearch.common.settings.Settings
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.Client

internal class SecureClientWrapperTests {

    private fun createMockClient(threadContext: ThreadContext): Client {
        val client = mock(Client::class.java)
        val threadPool = mock(ThreadPool::class.java)
        `when`(client.threadPool()).thenReturn(threadPool)
        `when`(threadPool.threadContext).thenReturn(threadContext)
        return client
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test tenant id header preserved across stash in execute`() {
        val threadContext = ThreadContext(Settings.EMPTY)
        val client = createMockClient(threadContext)
        val wrapper = SecureClientWrapper(client)

        threadContext.putHeader(SecureClientWrapper.TENANT_ID_HEADER, "tenant-123")

        var capturedTenantId: String? = null
        `when`(client.execute(any<ActionType<ActionResponse>>(), any(), any<ActionListener<ActionResponse>>())).thenAnswer {
            capturedTenantId = threadContext.getHeader(SecureClientWrapper.TENANT_ID_HEADER)
            null
        }

        wrapper.execute(
            mock(ActionType::class.java) as ActionType<ActionResponse>,
            mock(org.opensearch.action.ActionRequest::class.java),
            mock(ActionListener::class.java) as ActionListener<ActionResponse>
        )

        assertEquals("tenant-123", capturedTenantId)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test security context is still stashed`() {
        val threadContext = ThreadContext(Settings.EMPTY)
        val client = createMockClient(threadContext)
        val wrapper = SecureClientWrapper(client)

        threadContext.putHeader("_opendistro_security_user", "admin|backend_role")
        threadContext.putHeader(SecureClientWrapper.TENANT_ID_HEADER, "tenant-abc")

        var capturedSecurityHeader: String? = "not-null-sentinel"
        var capturedTenantId: String? = null
        `when`(client.execute(any<ActionType<ActionResponse>>(), any(), any<ActionListener<ActionResponse>>())).thenAnswer {
            capturedSecurityHeader = threadContext.getHeader("_opendistro_security_user")
            capturedTenantId = threadContext.getHeader(SecureClientWrapper.TENANT_ID_HEADER)
            null
        }

        wrapper.execute(
            mock(ActionType::class.java) as ActionType<ActionResponse>,
            mock(org.opensearch.action.ActionRequest::class.java),
            mock(ActionListener::class.java) as ActionListener<ActionResponse>
        )

        assertNull(capturedSecurityHeader)
        assertEquals("tenant-abc", capturedTenantId)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test no tenant id header does not inject null`() {
        val threadContext = ThreadContext(Settings.EMPTY)
        val client = createMockClient(threadContext)
        val wrapper = SecureClientWrapper(client)

        var capturedTenantId: String? = "sentinel"
        `when`(client.execute(any<ActionType<ActionResponse>>(), any(), any<ActionListener<ActionResponse>>())).thenAnswer {
            capturedTenantId = threadContext.getHeader(SecureClientWrapper.TENANT_ID_HEADER)
            null
        }

        wrapper.execute(
            mock(ActionType::class.java) as ActionType<ActionResponse>,
            mock(org.opensearch.action.ActionRequest::class.java),
            mock(ActionListener::class.java) as ActionListener<ActionResponse>
        )

        assertNull(capturedTenantId)
    }
}
