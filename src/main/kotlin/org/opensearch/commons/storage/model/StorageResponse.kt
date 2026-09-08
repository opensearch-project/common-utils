/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.model

import org.opensearch.commons.storage.api.StorageEngine
import org.opensearch.commons.storage.api.StorageOperation
import org.opensearch.commons.storage.error.StorageError

/**
 * StorageResponse represents the response returned after handling a StorageRequest
 * @param <T> The type of the payload.
 */
data class StorageResponse<T> (
    val payload: T? = null,
    val operation: StorageOperation,
    val engine: StorageEngine,
    val error: StorageError? = null
)
