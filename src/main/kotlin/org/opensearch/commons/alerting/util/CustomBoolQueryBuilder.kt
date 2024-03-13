package org.opensearch.commons.alerting.util

import org.apache.lucene.search.join.ScoreMode
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilders

class CustomBoolQueryBuilder : BoolQueryBuilder(), Writeable {

    companion object {
        @JvmStatic
        fun fromStreamInput(sin: StreamInput): CustomBoolQueryBuilder {
            return CustomBoolQueryBuilder().apply {
                // Deserialize BoolQueryBuilder fields here
                // For example:
                if (sin.readBoolean()) {
                    val detectionType = sin.readString()
                    // Construct nested query based on detection type
                    // For example:
                     if (detectionType.equals("threat")) {
                         this.must(QueryBuilders.prefixQuery("queries.id", "threat_intel_"))
                     } else {
                         this.mustNot(QueryBuilders.prefixQuery("queries.id", "threat_intel_"))
                     }
                }

                // Deserialize finding IDs
                if (sin.readBoolean()) {
                    // Construct terms query for finding IDs
                    // For example:
                    this.filter(QueryBuilders.termsQuery("id", sin.readList<String> { it.readString() }))
                }

                // Deserialize time range
                if (sin.readBoolean()) {
                    val startTimeMillis = sin.readLong()
                    val endTimeMillis = sin.readLong()
                    // Construct range query for time range
                    // For example:
                     this.filter(QueryBuilders.rangeQuery("timestamp").from(startTimeMillis).to(endTimeMillis))
                }

                // Deserialize severity
                if (sin.readBoolean()) {
                    val severity = sin.readString()
                    // Construct nested query for severity
                    // For example:
                     this.must(QueryBuilders.nestedQuery("queries", QueryBuilders.matchQuery("queries.tags", severity), ScoreMode.None))
                }
            }
        }
    }
}
