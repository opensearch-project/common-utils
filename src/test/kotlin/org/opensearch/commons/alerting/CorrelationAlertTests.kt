package org.opensearch.commons.alerting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.model.CorrelationAlert
import org.opensearch.commons.utils.recreateObject
import java.time.temporal.ChronoUnit

class CorrelationAlertTests {

    @Test
    fun `test correlation alert as template args`() {
        // Create sample data for CorrelationAlert
        val correlationAlert = randomCorrelationAlert("alertId1", Alert.State.ACTIVE)

        // Generate template args using asTemplateArg() function
        val templateArgs = createCorrelationAlertTemplateArgs(correlationAlert)

        assertEquals(
            templateArgs["correlated_finding_ids"],
            correlationAlert.correlatedFindingIds,
            "Template args correlatedFindingIds does not match"
        )
        assertEquals(
            templateArgs["correlation_rule_id"],
            correlationAlert.correlationRuleId,
            "Template args correlationRuleId does not match"
        )
        assertEquals(
            templateArgs["correlation_rule_name"],
            correlationAlert.correlationRuleName,
            "Template args correlationRuleName does not match"
        )

        // Verify inherited properties from BaseAlert
        assertEquals(templateArgs["id"], correlationAlert.id, "alertId1")
        assertEquals(templateArgs["version"], correlationAlert.version, "Template args version does not match")
        assertEquals(templateArgs["user"], correlationAlert.user, "Template args user does not match")
        assertEquals(
            templateArgs["trigger_name"],
            correlationAlert.triggerName,
            "Template args triggerName does not match"
        )
        assertEquals(templateArgs["state"], correlationAlert.state, "Template args state does not match")
        assertEquals(templateArgs["start_time"], correlationAlert.startTime, "Template args startTime does not match")
        assertEquals(templateArgs["end_time"], correlationAlert.endTime, "Template args endTime does not match")
        assertEquals(
            templateArgs["acknowledged_time"],
            correlationAlert.acknowledgedTime,
            "Template args acknowledgedTime does not match"
        )
        assertEquals(
            templateArgs["error_message"],
            correlationAlert.errorMessage,
            "Template args errorMessage does not match"
        )
        assertEquals(templateArgs["severity"], correlationAlert.severity, "Template args severity does not match")
        assertEquals(
            templateArgs["action_execution_results"],
            correlationAlert.actionExecutionResults,
            "Template args actionExecutionResults does not match"
        )
    }

    @Test
    fun `test alert acknowledged`() {
        val ackCorrelationAlert = randomCorrelationAlert("alertId1", Alert.State.ACKNOWLEDGED)
        Assertions.assertTrue(ackCorrelationAlert.isAcknowledged(), "Alert is not acknowledged")

        val activeCorrelationAlert = randomCorrelationAlert("alertId1", Alert.State.ACTIVE)
        Assertions.assertFalse(activeCorrelationAlert.isAcknowledged(), "Alert is acknowledged")
    }

    @Test
    fun `Feature Correlation Alert serialize and deserialize should be equal`() {
        val correlationAlert = randomCorrelationAlert("alertId1", Alert.State.ACTIVE)
        val recreatedAlert = recreateObject(correlationAlert) { CorrelationAlert(it) }
        assertEquals(correlationAlert.correlatedFindingIds, recreatedAlert.correlatedFindingIds)
        assertEquals(correlationAlert.correlationRuleId, recreatedAlert.correlationRuleId)
        assertEquals(correlationAlert.correlationRuleName, recreatedAlert.correlationRuleName)
        assertEquals(correlationAlert.triggerName, recreatedAlert.triggerName)
        assertEquals(correlationAlert.state, recreatedAlert.state)
        val expectedStartTime = correlationAlert.startTime.truncatedTo(ChronoUnit.MILLIS)
        val actualStartTime = recreatedAlert.startTime.truncatedTo(ChronoUnit.MILLIS)
        assertEquals(expectedStartTime, actualStartTime)
        assertEquals(correlationAlert.severity, recreatedAlert.severity)
        assertEquals(correlationAlert.id, recreatedAlert.id)
        assertEquals(correlationAlert.actionExecutionResults, recreatedAlert.actionExecutionResults)
    }
}
