/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.opensearch.core.common.io.stream.StreamInput

fun StreamInput.readMapAsMutableMap(): MutableMap<String, Any> =
    readMap()?.toMutableMap() ?: mutableMapOf()
