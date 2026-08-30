/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.error

/**
 * Represents a storage-related error that can be returned as part of a [StorageResponse].
 */
sealed class StorageError {
    data class Generic(val message: String, val cause: Throwable? = null) : StorageError()
}
