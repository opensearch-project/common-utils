package org.opensearch.commons.alerting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.XContentHelper
import org.opensearch.commons.alerting.model.Alert
import org.opensearch.commons.alerting.model.CorrelationAlert
import org.opensearch.commons.utils.getJsonString
import org.opensearch.core.common.bytes.BytesArray
import org.opensearch.core.common.bytes.BytesReference
import org.opensearch.core.common.io.stream.InputStreamStreamInput
import org.opensearch.core.xcontent.NamedXContentRegistry
import java.time.temporal.ChronoUnit

class CorrelationAlertTests {

    @Test
    fun `test correlation alert as template args`() {
        // Create sample data for CorrelationAlert
        val correlationAlert = randomCorrelationAlert("alertId1", Alert.State.ACTIVE)

        // Generate template args using asTemplateArg() function
        val templateArgs = createCorrelationAlertTemplateArgs(correlationAlert)

        assertEquals(
            templateArgs["correlatedFindingIds"],
            correlationAlert.correlatedFindingIds,
            "Template args correlatedFindingIds does not match"
        )
        assertEquals(
            templateArgs["correlationRuleId"],
            correlationAlert.correlationRuleId,
            "Template args correlationRuleId does not match"
        )
        assertEquals(
            templateArgs["correlationRuleName"],
            correlationAlert.correlationRuleName,
            "Template args correlationRuleName does not match"
        )

        // Verify inherited properties from UnifiedAlert
        assertEquals(templateArgs["id"], correlationAlert.id, "alertId1")
        assertEquals(templateArgs["version"], correlationAlert.version, "Template args version does not match")
        assertEquals(templateArgs["user"], correlationAlert.user, "Template args user does not match")
        assertEquals(
            templateArgs["triggerName"],
            correlationAlert.triggerName,
            "Template args triggerName does not match"
        )
        assertEquals(templateArgs["state"], correlationAlert.state, "Template args state does not match")
        assertEquals(templateArgs["startTime"], correlationAlert.startTime, "Template args startTime does not match")
        assertEquals(templateArgs["endTime"], correlationAlert.endTime, "Template args endTime does not match")
        assertEquals(
            templateArgs["acknowledgedTime"],
            correlationAlert.acknowledgedTime,
            "Template args acknowledgedTime does not match"
        )
        assertEquals(
            templateArgs["errorMessage"],
            correlationAlert.errorMessage,
            "Template args errorMessage does not match"
        )
        assertEquals(templateArgs["severity"], correlationAlert.severity, "Template args severity does not match")
        assertEquals(
            templateArgs["actionExecutionResults"],
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
    fun `test correlation parse function`() {
        // Generate a random CorrelationAlert object
        val correlationAlert = randomCorrelationAlert("alertId1", Alert.State.ACTIVE)
        val correlationAlertString = getJsonString(correlationAlert)

        // Convert the JSON string to a BytesReference
        val serializedBytes: BytesReference = BytesArray(correlationAlertString.toByteArray(Charsets.UTF_8))

        // Deserialize the BytesReference into a CorrelationAlert object using the parse function
        val recreatedAlert: CorrelationAlert = InputStreamStreamInput(serializedBytes.streamInput()).use { streamInput ->
            XContentHelper.createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, serializedBytes).use { parser ->
                parser.nextToken() // Move to the start of the content
                CorrelationAlert.parse(parser)
            }
        }

        // Assert that the deserialized object matches the original object
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
