package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.alerting.randomDocLevelMonitorInput
import org.opensearch.commons.alerting.randomDocLevelQuery
import org.opensearch.commons.alerting.util.string
import java.lang.IllegalArgumentException

class DocLevelMonitorInputTests {
    @Test
    fun `test DocLevelQuery asTemplateArgs`() {
        // GIVEN
        val query = randomDocLevelQuery()

        // WHEN
        val templateArgs = query.asTemplateArg()

        // THEN
        Assertions.assertEquals(
            templateArgs[DocLevelQuery.QUERY_ID_FIELD],
            query.id,
            "Template args 'id' field does not match:"
        )
        Assertions.assertEquals(
            templateArgs[DocLevelQuery.QUERY_FIELD],
            query.query,
            "Template args 'query' field does not match:"
        )
        Assertions.assertEquals(
            templateArgs[DocLevelQuery.NAME_FIELD],
            query.name,
            "Template args 'name' field does not match:"
        )
        Assertions.assertEquals(
            templateArgs[DocLevelQuery.TAGS_FIELD],
            query.tags,
            "Template args 'tags' field does not match:"
        )
    }

    @Test
    fun `test create Doc Level Query with invalid characters for name`() {
        val badString = "query with space"
        try {
            randomDocLevelQuery(name = badString)
            Assertions.fail("Expecting an illegal argument exception")
        } catch (e: IllegalArgumentException) {
            Assertions.assertEquals(
                "They query name or tag, $badString, contains an invalid character: [' ','[',']','{','}','(',')']",
                e.message
            )
        }
    }

    @Test
    @Throws(IllegalArgumentException::class)
    fun `test create Doc Level Query with invalid characters for tags`() {
        val badString = "[(){}]"
        try {
            randomDocLevelQuery(tags = listOf(badString))
            Assertions.fail("Expecting an illegal argument exception")
        } catch (e: IllegalArgumentException) {
            Assertions.assertEquals(
                "They query name or tag, $badString, contains an invalid character: [' ','[',']','{','}','(',')']",
                e.message
            )
        }
    }

    @Test
    fun `test DocLevelMonitorInput asTemplateArgs`() {
        // GIVEN
        val input = randomDocLevelMonitorInput()

        // test
        input.toXContent(XContentBuilder.builder(XContentType.JSON.xContent()), ToXContent.EMPTY_PARAMS).string()
        // assertEquals("test", inputString)
        // test end
        // WHEN
        val templateArgs = input.asTemplateArg()

        // THEN
        Assertions.assertEquals(
            templateArgs[DocLevelMonitorInput.DESCRIPTION_FIELD],
            input.description,
            "Template args 'description' field does not match:"
        )
        Assertions.assertEquals(
            templateArgs[DocLevelMonitorInput.INDICES_FIELD],
            input.indices,
            "Template args 'indices' field does not match:"
        )
        Assertions.assertEquals(
            input.queries.size,
            (templateArgs[DocLevelMonitorInput.QUERIES_FIELD] as List<*>).size,
            "Template args 'queries' field does not contain the expected number of queries:"
        )
        input.queries.forEach {
            Assertions.assertTrue(
                (templateArgs[DocLevelMonitorInput.QUERIES_FIELD] as List<*>).contains(it.asTemplateArg()),
                "Template args 'queries' field does not match:"
            )
        }
    }
}
