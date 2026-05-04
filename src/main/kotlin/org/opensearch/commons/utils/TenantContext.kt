/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Coroutine context element that carries the tenant ID for the current request.
 * Set when launching a coroutine and read when building SDK requests.
 */
data class TenantContext(val tenantId: String?) : AbstractCoroutineContextElement(TenantContext) {
    companion object Key : CoroutineContext.Key<TenantContext>
}

/**
 * Retrieves the tenant ID from the current coroutine context.
 * Returns null if no [TenantContext] is present or if the tenant ID is not set.
 */
suspend fun currentTenantId(): String? = coroutineContext[TenantContext]?.tenantId
