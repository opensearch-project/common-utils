/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.randomAlertContext

class AlertContextTests {
    private var alertContext: AlertContext = randomAlertContext()

    @BeforeEach
    fun generateRandomData() {
        alertContext = randomAlertContext()
    }

    @Test
    fun `test AlertContext asTemplateArg`() {
        val templateArgs = alertContext.asTemplateArg()

        assertEquals(templateArgs[Alert.ALERT_ID_FIELD], alertContext.id, "Template args id does not match")
        assertEquals(templateArgs[Alert.ALERT_VERSION_FIELD], alertContext.version, "Template args version does not match")
        assertEquals(templateArgs[Alert.STATE_FIELD], alertContext.state.toString(), "Template args state does not match")
        assertEquals(templateArgs[Alert.ERROR_MESSAGE_FIELD], alertContext.errorMessage, "Template args error message does not match")
        assertEquals(templateArgs[Alert.ACKNOWLEDGED_TIME_FIELD], null, "Template args acknowledged time does not match")
        assertEquals(templateArgs[Alert.END_TIME_FIELD], alertContext.endTime?.toEpochMilli(), "Template args end time does not")
        assertEquals(templateArgs[Alert.START_TIME_FIELD], alertContext.startTime.toEpochMilli(), "Template args start time does not")
        assertEquals(templateArgs[Alert.LAST_NOTIFICATION_TIME_FIELD], null, "Template args last notification time does not match")
        assertEquals(templateArgs[Alert.SEVERITY_FIELD], alertContext.severity, "Template args severity does not match")
        assertEquals(templateArgs[Alert.CLUSTERS_FIELD], alertContext.clusters?.joinToString(","), "Template args clusters does not match")
        val formattedQueries = alertContext.associatedQueries?.map {
            mapOf(
                DocLevelQuery.QUERY_ID_FIELD to it.id,
                DocLevelQuery.NAME_FIELD to it.name,
                DocLevelQuery.TAGS_FIELD to it.tags
            )
        }
        assertEquals(templateArgs[AlertContext.ASSOCIATED_QUERIES_FIELD], formattedQueries, "Template associated queries do not match")
        assertEquals(templateArgs[AlertContext.SAMPLE_DOCS_FIELD], alertContext.sampleDocs, "Template args sample docs do not match")
    }
}
