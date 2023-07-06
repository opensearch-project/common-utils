/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class EventListenerResponse : BaseResponse {

    constructor() : super() {}

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this()

    override fun writeTo(out: StreamOutput) {
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder
    }
}
