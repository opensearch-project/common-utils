/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.model

/**
 * This model is a wrapper for [Alert] that should only be used to create a more
 * informative alert object to enrich mustache template notification messages.
 */
data class AlertContext(
    val alert: Alert,
    val associatedQueries: List<DocLevelQuery>? = null,
    val sampleDocs: List<Map<String, Any?>>? = null
) : Alert(
    id = alert.id,
    version = alert.version,
    schemaVersion = alert.schemaVersion,
    monitorId = alert.monitorId,
    monitorName = alert.monitorName,
    monitorVersion = alert.monitorVersion,
    monitorUser = alert.monitorUser,
    triggerId = alert.triggerId,
    triggerName = alert.triggerName,
    state = alert.state,
    startTime = alert.startTime,
    endTime = alert.endTime,
    lastNotificationTime = alert.lastNotificationTime,
    acknowledgedTime = alert.acknowledgedTime,
    errorMessage = alert.errorMessage,
    errorHistory = alert.errorHistory,
    severity = alert.severity,
    actionExecutionResults = alert.actionExecutionResults,
    aggregationResultBucket = alert.aggregationResultBucket,
    findingIds = alert.findingIds,
    relatedDocIds = alert.relatedDocIds,
    executionId = alert.executionId,
    workflowId = alert.workflowId,
    workflowName = alert.workflowName,
    associatedAlertIds = alert.associatedAlertIds,
    clusters = alert.clusters
) {

    override fun asTemplateArg(): Map<String, Any?> {
        val queriesContext = associatedQueries?.map {
            mapOf(
                DocLevelQuery.QUERY_ID_FIELD to it.id,
                DocLevelQuery.NAME_FIELD to it.name,
                DocLevelQuery.TAGS_FIELD to it.tags
            )
        }

        // Compile the custom context fields.
        val customContextFields = mapOf(
            ASSOCIATED_QUERIES_FIELD to queriesContext,
            SAMPLE_DOCS_FIELD to sampleDocs
        )

        // Get the alert template args
        val templateArgs = super.asTemplateArg().toMutableMap()

        // Add the non-null custom context fields to the alert templateArgs.
        customContextFields.forEach { (key, value) ->
            if (value !== null) templateArgs[key] = value
        }
        return templateArgs
    }

    companion object {
        const val ASSOCIATED_QUERIES_FIELD = "associated_queries"
        const val SAMPLE_DOCS_FIELD = "sample_documents"
    }
}
