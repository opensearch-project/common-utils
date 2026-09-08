/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.storage.api

/**
 * StorageAdapterFactory is used by the StorageService to get the StorageRepository
 * based on the provided StorageEngine.
 */
interface StorageAdapterFactory {
    /**
     * returns the StorageRepository based on the provided StorageEngine.
     *
     * @param engine The StorageEngine.
     * @return The StorageRepository
     */
    fun getAdapter(engine: StorageEngine): StorageRepository
}
