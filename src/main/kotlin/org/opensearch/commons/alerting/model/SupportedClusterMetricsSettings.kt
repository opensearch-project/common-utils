/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

import org.opensearch.common.xcontent.XContentHelper
import org.opensearch.common.xcontent.json.JsonXContent

/**
 * A class that supports storing a unique set of API paths that can be accessed by general users.
 */
class SupportedClusterMetricsSettings {
    companion object {
        const val RESOURCE_FILE = "supported_json_payloads.json"

        /**
         * The key in this map represents the path to call an API.
         *
         * NOTE: Paths should conform to the following pattern:
         * "/_cluster/stats"
         *
         * The value in these maps represents a path root mapped to a list of paths to field values.
         * If the value mapped to an API is an empty map, no fields will be redacted from the API response.
         *
         * NOTE: Keys in this map should consist of root components of the response body; e.g.,:
         * "indices"
         *
         * Values in these maps should consist of the remaining fields in the path
         * to the supported value separated by periods; e.g.,:
         * "shards.total",
         * "shards.index.shards.min"
         *
         * In this example for ClusterStats, the response will only include
         * the values at the end of these two paths:
         * "/_cluster/stats": {
         *      "indices": [
         *          "shards.total",
         *          "shards.index.shards.min"
         *      ]
         * }
         */
        private var supportedApiList = HashMap<String, Map<String, ArrayList<String>>>()

        init {
            val supportedJsonPayloads = SupportedClusterMetricsSettings::class.java.getResource(RESOURCE_FILE)

            @Suppress("UNCHECKED_CAST")
            if (supportedJsonPayloads != null)
                supportedApiList =
                    XContentHelper.convertToMap(JsonXContent.jsonXContent, supportedJsonPayloads.readText(), false)
                            as HashMap<String, Map<String, ArrayList<String>>>
        }


        /**
         * Confirms whether the provided path is in [supportedApiList].
         * Throws an exception if the provided path is not on the list; otherwise performs no action.
         * @param clusterMetricsInput The [ClusterMetricsInput] to validate.
         * @throws IllegalArgumentException when supportedApiList does not contain the provided path.
         */
        fun validateApiType(clusterMetricsInput: ClusterMetricsInput) {
            if (!supportedApiList.keys.contains(clusterMetricsInput.clusterMetricType.defaultPath))
                throw IllegalArgumentException("API path not in supportedApiList.")
        }
    }
}
