/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.Writeable

/**
 * Reads a raw (untyped) map from StreamInput and guarantees a mutable result.
 *
 * StreamInput.readMap() returns Collections.emptyMap() (immutable) for zero-entry maps.
 * Any code that casts that result to MutableMap without a defensive copy will throw
 * ClassCastException or UnsupportedOperationException at runtime.
 *
 * This extension wraps the result in a HashMap so callers always receive a mutable map,
 * regardless of whether the deserialized map was empty or not.
 */
fun StreamInput.readMapAsMutableMap(): MutableMap<String, Any> =
    readMap()?.let { HashMap(it) } ?: mutableMapOf()

/**
 * Reads a typed map from StreamInput using explicit key/value readers and guarantees
 * a mutable result.
 *
 * Replaces the repeated pattern:
 *   val raw = sin.readMap(keyReader, valueReader)
 *   if (raw.isEmpty()) return mutableMapOf()
 *   @Suppress("UNCHECKED_CAST")
 *   return HashMap(raw as Map<K, V>)
 *
 * Because StreamInput.readMap(keyReader, valueReader) already types the keys and values
 * at read time, the only remaining cast is from MutableMap<K?,V?> → MutableMap<K,V>, which
 * is safe provided the readers never produce null keys or values.
 */
@Suppress("UNCHECKED_CAST")
fun <K, V> StreamInput.readMapSafe(
    keyReader: Writeable.Reader<K>,
    valueReader: Writeable.Reader<V>
): MutableMap<K, V> {
    val raw = readMap(keyReader, valueReader)
    if (raw.isEmpty()) return mutableMapOf()
    return HashMap(raw as Map<K, V>)
}
