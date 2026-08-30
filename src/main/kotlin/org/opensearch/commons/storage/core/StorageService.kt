/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.core

import org.apache.logging.log4j.LogManager
import org.opensearch.commons.storage.api.StorageAdapterFactory
import org.opensearch.commons.storage.api.StorageOperation
import org.opensearch.commons.storage.model.StorageRequest
import org.opensearch.commons.storage.model.StorageResponse

/**
 * StorageService is the primary layer for handling requests from the clients and
 * delegates the requests to the appropriate storage adapter and returns the response.
 */
class StorageService(private val storageAdapterFactory: StorageAdapterFactory) {
    private val log = LogManager.getLogger(StorageService::class.java)

    suspend fun handleRequest(request: StorageRequest<Any>): StorageResponse<Any> {
        log.info(
            "[StorageService] Handling request: engine=${request.engine}, operation=${request.operation}"
        )

        val storageRepository = storageAdapterFactory.getAdapter(request.engine)
        return when (request.operation) {
            StorageOperation.SAVE -> storageRepository.save(request)
            StorageOperation.GET -> storageRepository.get(request)
            StorageOperation.SEARCH -> storageRepository.search(request)
            StorageOperation.UPDATE -> storageRepository.update(request)
            StorageOperation.DELETE -> storageRepository.delete(request)
        }
    }
}
