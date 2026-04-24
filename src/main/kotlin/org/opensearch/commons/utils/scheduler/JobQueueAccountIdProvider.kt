package org.opensearch.commons.utils.scheduler

import org.apache.logging.log4j.LogManager
import org.opensearch.common.settings.Settings
import java.util.ServiceLoader

/**
 * Provides AWS account IDs hosting job queues for polling.
 *
 * Implementations are discovered via [ServiceLoader]. Each implementation must:
 * - Have a public no-arg constructor
 * - Declare itself in META-INF/services/org.opensearch.commons.utils.scheduler.JobQueueAccountIdProvider
 * - Return a unique [getType] string
 * - Accept configuration via [initialize] before [getAccountIds] is called
 */
interface JobQueueAccountIdProvider {
    /** Identifier used to select this provider via configuration. */
    fun getType(): String

    /**
     * Initialize this provider with node settings. Called once after discovery
     * and before [getAccountIds].
     */
    fun initialize(settings: Settings)

    fun getAccountIds(): List<String>

    companion object {
        private val log = LogManager.getLogger(JobQueueAccountIdProvider::class.java)

        /**
         * Find a provider matching [providerType], initialize it with [settings],
         * and return it ready to use.
         */
        @JvmStatic
        fun find(providerType: String, settings: Settings): JobQueueAccountIdProvider {
            val loader = ServiceLoader.load(JobQueueAccountIdProvider::class.java, JobQueueAccountIdProvider::class.java.classLoader)

            for (provider in loader) {
                log.info("Discovered JobQueueAccountIdProvider: [{}]", provider.getType())
                if (provider.getType() == providerType) {
                    log.info("Found JobQueueAccountIdProvider for type [{}]", providerType)
                    provider.initialize(settings)
                    return provider
                }
            }
            throw IllegalArgumentException("No JobQueueAccountIdProvider found for type [$providerType]")
        }
    }
}
