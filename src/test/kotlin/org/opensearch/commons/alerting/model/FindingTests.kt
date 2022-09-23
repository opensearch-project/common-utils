package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.randomFinding

internal class FindingTests {
    @Test
    fun `test finding asTemplateArgs`() {
        // GIVEN
        val finding = randomFinding()

        // WHEN
        val templateArgs = finding.asTemplateArg()

        // THEN
        assertEquals(templateArgs[Finding.FINDING_ID_FIELD], finding.id, "Template args 'id' field does not match:")
        assertEquals(
            templateArgs[Finding.RELATED_DOC_IDS_FIELD],
            finding.relatedDocIds,
            "Template args 'relatedDocIds' field does not match:"
        )
        assertEquals(templateArgs[Finding.MONITOR_ID_FIELD], finding.monitorId, "Template args 'monitorId' field does not match:")
        assertEquals(
            templateArgs[Finding.MONITOR_NAME_FIELD],
            finding.monitorName,
            "Template args 'monitorName' field does not match:",
        )
        assertEquals(
            templateArgs[Finding.QUERIES_FIELD],
            finding.docLevelQueries,
            "Template args 'queries' field does not match:"
        )
        assertEquals(
            templateArgs[Finding.TIMESTAMP_FIELD],
            finding.timestamp.toEpochMilli(),
            "Template args 'timestamp' field does not match:"
        )
    }
}
