package org.opensearch.commons.alerting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.Alert
import java.time.Instant
import java.time.temporal.ChronoUnit

class AlertTests {
    @Test
    fun `test alert as template args`() {
        val alert = randomAlert().copy(acknowledgedTime = null, lastNotificationTime = null)

        val templateArgs = alert.asTemplateArg()

        assertEquals(templateArgs[Alert.ALERT_ID_FIELD], alert.id, "Template args id does not match")
        assertEquals(templateArgs[Alert.ALERT_VERSION_FIELD], alert.version, "Template args version does not match")
        assertEquals(templateArgs[Alert.STATE_FIELD], alert.state.toString(), "Template args state does not match")
        assertEquals(templateArgs[Alert.ERROR_MESSAGE_FIELD], alert.errorMessage, "Template args error message does not match")
        assertEquals(templateArgs[Alert.ACKNOWLEDGED_TIME_FIELD], null, "Template args acknowledged time does not match")
        assertEquals(templateArgs[Alert.END_TIME_FIELD], alert.endTime?.toEpochMilli(), "Template args end time does not")
        assertEquals(templateArgs[Alert.START_TIME_FIELD], alert.startTime.toEpochMilli(), "Template args start time does not")
        assertEquals(templateArgs[Alert.LAST_NOTIFICATION_TIME_FIELD], null, "Template args last notification time does not match")
        assertEquals(templateArgs[Alert.SEVERITY_FIELD], alert.severity, "Template args severity does not match")
        assertEquals(templateArgs[Alert.CLUSTERS_FIELD], alert.clusters?.joinToString(","), "Template args clusters does not match")
    }

    @Test
    fun `test agg alert as template args`() {
        val alert = randomAlertWithAggregationResultBucket().copy(acknowledgedTime = null, lastNotificationTime = null)

        val templateArgs = alert.asTemplateArg()

        assertEquals(templateArgs[Alert.ALERT_ID_FIELD], alert.id, "Template args id does not match")
        assertEquals(templateArgs[Alert.ALERT_VERSION_FIELD], alert.version, "Template args version does not match")
        assertEquals(templateArgs[Alert.STATE_FIELD], alert.state.toString(), "Template args state does not match")
        assertEquals(templateArgs[Alert.ERROR_MESSAGE_FIELD], alert.errorMessage, "Template args error message does not match")
        assertEquals(templateArgs[Alert.ACKNOWLEDGED_TIME_FIELD], null, "Template args acknowledged time does not match")
        assertEquals(templateArgs[Alert.END_TIME_FIELD], alert.endTime?.toEpochMilli(), "Template args end time does not")
        assertEquals(templateArgs[Alert.START_TIME_FIELD], alert.startTime.toEpochMilli(), "Template args start time does not")
        assertEquals(templateArgs[Alert.LAST_NOTIFICATION_TIME_FIELD], null, "Template args last notification time does not match")
        assertEquals(templateArgs[Alert.SEVERITY_FIELD], alert.severity, "Template args severity does not match")
        assertEquals(templateArgs[Alert.CLUSTERS_FIELD], alert.clusters?.joinToString(","), "Template args clusters does not match")
        assertEquals(
            templateArgs[Alert.BUCKET_KEYS],
            alert.aggregationResultBucket?.bucketKeys?.joinToString(","),
            "Template args bucketKeys do not match"
        )
        assertEquals(
            templateArgs[Alert.PARENTS_BUCKET_PATH],
            alert.aggregationResultBucket?.parentBucketPath,
            "Template args parentBucketPath does not match"
        )
    }

    @Test
    fun `test alert acknowledged`() {
        val ackAlert = randomAlert().copy(state = Alert.State.ACKNOWLEDGED)
        Assertions.assertTrue(ackAlert.isAcknowledged(), "Alert is not acknowledged")

        val activeAlert = randomAlert().copy(state = Alert.State.ACTIVE)
        Assertions.assertFalse(activeAlert.isAcknowledged(), "Alert is acknowledged")
    }

    @Test
    fun `test alert in audit state`() {
        val auditAlert = Alert(
            randomQueryLevelMonitor(),
            randomQueryLevelTrigger(),
            Instant.now().truncatedTo(ChronoUnit.MILLIS),
            null,
            actionExecutionResults = listOf(randomActionExecutionResult())
        )
        Assertions.assertFalse(auditAlert.isAcknowledged(), "Alert should not be in acknowledged state")
    }

    @Test
    fun `test chained alert`() {
        val workflow = randomWorkflow()
        val trigger = randomChainedAlertTrigger()
        val alert = randomChainedAlert(workflow = workflow, trigger = trigger)
        assertEquals(alert.monitorId, "")
        assertEquals(alert.id, "")
        assertEquals(workflow.id, alert.workflowId)
    }

    @Test
    fun `test alert copy`() {
        val alert = randomAlert()

        val copiedAlert = alert.copy()

        assertEquals(alert.id, copiedAlert.id)
        assertEquals(alert.version, copiedAlert.version)
        assertEquals(alert.schemaVersion, copiedAlert.schemaVersion)
        assertEquals(alert.monitorId, copiedAlert.monitorId)
        assertEquals(alert.workflowId, copiedAlert.workflowId)
        assertEquals(alert.workflowName, copiedAlert.workflowName)
        assertEquals(alert.monitorName, copiedAlert.monitorName)
        assertEquals(alert.monitorVersion, copiedAlert.monitorVersion)
        assertEquals(alert.monitorUser, copiedAlert.monitorUser)
        assertEquals(alert.triggerId, copiedAlert.triggerId)
        assertEquals(alert.triggerName, copiedAlert.triggerName)
        assertEquals(alert.findingIds, copiedAlert.findingIds)
        assertEquals(alert.relatedDocIds, copiedAlert.relatedDocIds)
        assertEquals(alert.state, copiedAlert.state)
        assertEquals(alert.startTime, copiedAlert.startTime)
        assertEquals(alert.endTime, copiedAlert.endTime)
        assertEquals(alert.lastNotificationTime, copiedAlert.lastNotificationTime)
        assertEquals(alert.acknowledgedTime, copiedAlert.acknowledgedTime)
        assertEquals(alert.errorMessage, copiedAlert.errorMessage)
        assertEquals(alert.errorHistory, copiedAlert.errorHistory)
        assertEquals(alert.severity, copiedAlert.severity)
        assertEquals(alert.actionExecutionResults, copiedAlert.actionExecutionResults)
        assertEquals(alert.aggregationResultBucket, copiedAlert.aggregationResultBucket)
        assertEquals(alert.executionId, copiedAlert.executionId)
        assertEquals(alert.associatedAlertIds, copiedAlert.associatedAlertIds)
        assertEquals(alert.clusters, copiedAlert.clusters)
    }

    @Test
    fun `test alert copy with modified properties`() {
        val alert = randomAlert()
        val newAlertValues = randomAlert()

        val alertCopy = alert.copy(
            id = newAlertValues.id,
            triggerId = newAlertValues.triggerId,
            triggerName = newAlertValues.triggerName,
            actionExecutionResults = newAlertValues.actionExecutionResults,
            clusters = newAlertValues.clusters
        )

        // Modified properties; compare to newAlertValues
        assertEquals(newAlertValues.id, alertCopy.id)
        assertEquals(newAlertValues.triggerId, alertCopy.triggerId)
        assertEquals(newAlertValues.triggerName, alertCopy.triggerName)
        assertEquals(newAlertValues.actionExecutionResults, alertCopy.actionExecutionResults)
        assertEquals(newAlertValues.clusters, alertCopy.clusters)

        // Retained values; compare to original alert
        assertEquals(alert.version, alertCopy.version)
        assertEquals(alert.schemaVersion, alertCopy.schemaVersion)
        assertEquals(alert.monitorId, alertCopy.monitorId)
        assertEquals(alert.workflowId, alertCopy.workflowId)
        assertEquals(alert.workflowName, alertCopy.workflowName)
        assertEquals(alert.monitorName, alertCopy.monitorName)
        assertEquals(alert.monitorVersion, alertCopy.monitorVersion)
        assertEquals(alert.monitorUser, alertCopy.monitorUser)
        assertEquals(alert.findingIds, alertCopy.findingIds)
        assertEquals(alert.relatedDocIds, alertCopy.relatedDocIds)
        assertEquals(alert.state, alertCopy.state)
        assertEquals(alert.startTime, alertCopy.startTime)
        assertEquals(alert.endTime, alertCopy.endTime)
        assertEquals(alert.lastNotificationTime, alertCopy.lastNotificationTime)
        assertEquals(alert.acknowledgedTime, alertCopy.acknowledgedTime)
        assertEquals(alert.errorMessage, alertCopy.errorMessage)
        assertEquals(alert.errorHistory, alertCopy.errorHistory)
        assertEquals(alert.severity, alertCopy.severity)
        assertEquals(alert.aggregationResultBucket, alertCopy.aggregationResultBucket)
        assertEquals(alert.executionId, alertCopy.executionId)
        assertEquals(alert.associatedAlertIds, alertCopy.associatedAlertIds)
    }

    @Test
    fun `test alert equals with duplicate alerts`() {
        val alert = randomAlert()
        val alertCopy = alert.copy()

        val alertsMatch = alert.equals(alertCopy)

        assertTrue(alertsMatch)
    }

    @Test
    fun `test alert equals with different alerts`() {
        val alert = randomAlert()
        val newAlertValues = randomAlert()
        val alertCopy = alert.copy(
            id = newAlertValues.id,
            triggerId = newAlertValues.triggerId,
            triggerName = newAlertValues.triggerName,
            actionExecutionResults = newAlertValues.actionExecutionResults,
            clusters = newAlertValues.clusters
        )

        val alertsMatch = alert.equals(alertCopy)

        assertFalse(alertsMatch)
    }

    @Test
    fun `test alert equals with null alert`() {
        val alert = randomAlert()

        val alertsMatch = alert.equals(null)

        assertFalse(alertsMatch)
    }

    @Test
    fun `test alert equals with alertContext`() {
        val alert = randomAlert()
        val alertContext = randomAlertContext(alert = alert)

        val alertsMatch = alert.equals(alertContext)

        assertFalse(alertsMatch)
    }
}
