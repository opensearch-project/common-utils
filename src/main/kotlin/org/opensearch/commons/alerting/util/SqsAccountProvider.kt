package org.opensearch.commons.alerting.util

/**
 * Provides the list of AWS account IDs that host SQS queues available for polling.
 */
fun interface SqsAccountProvider {
    fun getAccountIds(): List<String>
}
