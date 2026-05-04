/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

internal class TenantContextTests {

    private fun runSuspend(context: kotlin.coroutines.CoroutineContext = EmptyCoroutineContext, block: suspend () -> Unit) {
        var result: Result<Unit>? = null
        block.startCoroutine(object : kotlin.coroutines.Continuation<Unit> {
            override val context = context
            override fun resumeWith(r: Result<Unit>) { result = r }
        })
        result!!.getOrThrow()
    }

    @Test
    fun `test currentTenantId returns tenant id when set`() {
        runSuspend(TenantContext("test-tenant")) {
            assertEquals("test-tenant", currentTenantId())
        }
    }

    @Test
    fun `test currentTenantId returns null for single tenant deployment`() {
        runSuspend {
            assertNull(currentTenantId())
        }
    }

    @Test
    fun `test currentTenantId returns null when tenant id header is absent`() {
        runSuspend(TenantContext(null)) {
            assertNull(currentTenantId())
        }
    }
}
