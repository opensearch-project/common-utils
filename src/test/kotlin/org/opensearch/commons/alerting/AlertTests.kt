package org.opensearch.commons.alerting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.Alert

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
        assertEquals(
            templateArgs[Alert.BUCKET_KEYS],
            alert.aggregationResultBucket?.bucketKeys?.joinToString(","),
            "Template args bucketKeys do not match"
        )
        assertEquals(
            templateArgs[Alert.PARENTS_BUCKET_PATH],
            alert.aggregationResultBucket?.parentBucketPath,
            "Template args parentBucketPath does not match",
        )
    }

    @Test
    fun `test alert acknowledged`() {
        val ackAlert = randomAlert().copy(state = Alert.State.ACKNOWLEDGED)
        Assertions.assertTrue(ackAlert.isAcknowledged(), "Alert is not acknowledged")

        val activeAlert = randomAlert().copy(state = Alert.State.ACTIVE)
        Assertions.assertFalse(activeAlert.isAcknowledged(), "Alert is acknowledged")
    }
}
