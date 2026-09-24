/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.opensearch.core.common.io.stream.StreamInput

/**
 * Reads a map from StreamInput and guarantees a mutable result.
 * StreamInput.readMap() returns Collections.emptyMap() (immutable) for zero-size maps.
 * This extension safely converts that to a mutable LinkedHashMap.
 */
fun StreamInput.readMapAsMutableMap(): MutableMap<String, Any> =
    readMap()?.toMutableMap() ?: mutableMapOf()
