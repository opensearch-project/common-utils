/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContentObject

/**
 * interface for representing objects.
 */
interface BaseModel :
    Writeable,
    ToXContentObject
