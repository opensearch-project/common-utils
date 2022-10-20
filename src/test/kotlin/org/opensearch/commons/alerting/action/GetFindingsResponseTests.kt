package org.opensearch.commons.alerting.action

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.commons.alerting.model.DocLevelQuery
import org.opensearch.commons.alerting.model.Finding
import org.opensearch.commons.alerting.model.FindingDocument
import org.opensearch.commons.alerting.model.FindingWithDocs
import org.opensearch.rest.RestStatus
import java.time.Instant
import java.util.List

internal class GetFindingsResponseTests {

    @Test
    fun `test get findings response`() {

        // Alerting GetFindingsResponse mock #1
        val finding1 = Finding(
            "1",
            listOf("doc1", "doc2", "doc3"),
            "monitor_id1",
            "monitor_name1",
            "test_index1",
            listOf(DocLevelQuery("1", "myQuery", "fieldA:valABC", List.of())),
            Instant.now()
        )
        val findingDocument1 = FindingDocument("test_index1", "doc1", true, "document 1 payload")
        val findingDocument2 = FindingDocument("test_index1", "doc2", true, "document 2 payload")
        val findingDocument3 = FindingDocument("test_index1", "doc3", true, "document 3 payload")

        val findingWithDocs1 = FindingWithDocs(finding1, listOf(findingDocument1, findingDocument2, findingDocument3))

        // Alerting GetFindingsResponse mock #2

        // Alerting GetFindingsResponse mock #2
        val finding2 = Finding(
            "1",
            listOf("doc21", "doc22"),
            "monitor_id2",
            "monitor_name2",
            "test_index2",
            listOf(DocLevelQuery("1", "myQuery", "fieldA:valABC", List.of())),
            Instant.now()
        )
        val findingDocument21 = FindingDocument("test_index2", "doc21", true, "document 21 payload")
        val findingDocument22 = FindingDocument("test_index2", "doc22", true, "document 22 payload")

        val findingWithDocs2 = FindingWithDocs(finding2, listOf(findingDocument21, findingDocument22))

        val req = GetFindingsResponse(RestStatus.OK, 2, listOf(findingWithDocs1, findingWithDocs2))
        Assertions.assertNotNull(req)

        val out = BytesStreamOutput()
        req.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = GetFindingsResponse(sin)
    }
}
