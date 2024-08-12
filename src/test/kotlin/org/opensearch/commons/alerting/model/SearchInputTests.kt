package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.SearchInput.Companion.INDICES_FIELD
import org.opensearch.commons.alerting.model.SearchInput.Companion.QUERY_FIELD
import org.opensearch.commons.alerting.model.SearchInput.Companion.SEARCH_FIELD
import org.opensearch.commons.alerting.randomSearchInput
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SearchInputTests {

    @Test
    fun `test SearchInput asTemplateArgs`() {
        val searchInput = randomSearchInput()

        val templateArgs = searchInput.asTemplateArg()

        val search = templateArgs[SEARCH_FIELD] as? Map<*, *>
        assertNotNull(search, "Template arg field 'search' is empty")
        assertEquals(
            searchInput.indices,
            search[INDICES_FIELD],
            "Template arg field 'indices' doesn't match"
        )
        assertEquals(
            searchInput.query.toString(),
            search[QUERY_FIELD],
            "Template arg field 'query' doesn't match"
        )
    }
}
