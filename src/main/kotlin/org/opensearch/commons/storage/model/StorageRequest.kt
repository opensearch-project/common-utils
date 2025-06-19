/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.model

import org.opensearch.commons.storage.api.StorageEngine
import org.opensearch.commons.storage.api.StorageOperation

/**
 * StorageRequest represents a request to the storage service.
 * @param <T> The type of the payload.
 */
data class StorageRequest<T> (
    val payload: T? = null,
    val options: Any? = null,
    val operation: StorageOperation,
    val engine: StorageEngine
)
