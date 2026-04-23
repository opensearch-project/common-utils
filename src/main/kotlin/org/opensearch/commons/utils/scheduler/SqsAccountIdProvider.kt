package org.opensearch.commons.utils.scheduler

import org.apache.logging.log4j.LogManager
import java.util.ServiceLoader

/**
 * Provides AWS account IDs hosting SQS queues for polling.
 *
 * Implementations are discovered via [ServiceLoader]. Each implementation must:
 * - Have a public no-arg constructor
 * - Declare itself in META-INF/services/org.opensearch.commons.utils.scheduler.SqsAccountIdProvider
 * - Return a unique [type] string
 */
interface SqsAccountIdProvider {
    /** Identifier used to select this provider via configuration. */
    val type: String

    fun getAccountIds(): List<String>

    companion object {
        private val log = LogManager.getLogger(SqsAccountIdProvider::class.java)

        /**
         * Find a provider matching [providerType] using the given [classLoader].
         * Returns null if no match is found.
         */
        @JvmStatic
        fun find(providerType: String): SqsAccountIdProvider {
            val loader = ServiceLoader.load(SqsAccountIdProvider::class.java, SqsAccountIdProvider::class.java.classLoader)

            for (provider in loader) {
                log.info("Discovered SqsAccountIdProvider: [{}]", provider.type)
                if (provider.type == providerType) {
                    log.info("Found SqsAccountIdProvider for type [{}]", providerType)
                    return provider
                }
            }
            throw IllegalArgumentException("No SqsAccountIdProvider found for type [$providerType]")
        }
    }
}
