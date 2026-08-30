/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.api

import org.opensearch.commons.storage.model.StorageRequest
import org.opensearch.commons.storage.model.StorageResponse

/**
 * The StorageRepository Interface defines the basic operations such as saving,
 *  retrieving, searching, updating, and deleting data for the storage accessor.
 */
interface StorageRepository {
    suspend fun save(request: StorageRequest<Any>): StorageResponse<Any>
    suspend fun get(request: StorageRequest<Any>): StorageResponse<Any>
    suspend fun search(request: StorageRequest<Any>): StorageResponse<Any>
    suspend fun update(request: StorageRequest<Any>): StorageResponse<Any>
    suspend fun delete(request: StorageRequest<Any>): StorageResponse<Any>
}
