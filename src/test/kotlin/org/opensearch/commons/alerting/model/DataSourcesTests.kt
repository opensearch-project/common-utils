package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput

class DataSourcesTests {
    @Test
    fun `Test DataSources construction with no comments indices`() {
        val dataSources =
            DataSources(
                ScheduledJob.DOC_LEVEL_QUERIES_INDEX,
                ".opensearch-alerting-finding-history-write",
                "<.opensearch-alerting-finding-history-{now/d}-1>",
                ".opendistro-alerting-alerts",
                ".opendistro-alerting-alert-history-write",
                "<.opendistro-alerting-alert-history-{now/d}-1>",
                mapOf(),
                false,
            )
        Assertions.assertNotNull(dataSources)

        val out = BytesStreamOutput()
        dataSources.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDataSources = DataSources(sin)
        Assertions.assertEquals(ScheduledJob.DOC_LEVEL_QUERIES_INDEX, newDataSources.queryIndex)
        Assertions.assertEquals(".opensearch-alerting-finding-history-write", newDataSources.findingsIndex)
        Assertions.assertEquals("<.opensearch-alerting-finding-history-{now/d}-1>", newDataSources.findingsIndexPattern)
        Assertions.assertEquals(".opendistro-alerting-alerts", newDataSources.alertsIndex)
        Assertions.assertEquals(".opendistro-alerting-alert-history-write", newDataSources.alertsHistoryIndex)
        Assertions.assertEquals("<.opendistro-alerting-alert-history-{now/d}-1>", newDataSources.alertsHistoryIndexPattern)
        Assertions.assertEquals(mapOf<String, Map<String, String>>(), newDataSources.queryIndexMappingsByType)
        Assertions.assertEquals(false, newDataSources.findingsEnabled)
    }
}
