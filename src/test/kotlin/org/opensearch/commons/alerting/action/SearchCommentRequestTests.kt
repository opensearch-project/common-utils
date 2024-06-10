package org.opensearch.commons.alerting.action

import org.opensearch.action.search.SearchRequest
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.unit.TimeValue
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.test.OpenSearchTestCase
import org.opensearch.test.rest.OpenSearchRestTestCase
import java.util.concurrent.TimeUnit

class SearchCommentRequestTests : OpenSearchTestCase() {
    fun `test search comments request`() {
        val searchSourceBuilder = SearchSourceBuilder().from(0).size(100).timeout(TimeValue(60, TimeUnit.SECONDS))
        val searchRequest = SearchRequest().indices(OpenSearchRestTestCase.randomAlphaOfLength(10)).source(searchSourceBuilder)
        val searchCommentRequest = SearchCommentRequest(searchRequest)
        assertNotNull(searchCommentRequest)

        val out = BytesStreamOutput()
        searchCommentRequest.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newReq = SearchCommentRequest(sin)

        assertNotNull(newReq.searchRequest)
        assertEquals(1, newReq.searchRequest.indices().size)
    }
}
