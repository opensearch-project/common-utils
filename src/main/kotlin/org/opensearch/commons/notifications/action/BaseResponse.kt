/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.notifications.action

import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.rest.RestStatus
import org.opensearch.core.xcontent.ToXContentObject
import java.io.IOException

/**
 * Base response which give REST status.
 */
abstract class BaseResponse :
    ActionResponse,
    ToXContentObject {
    /**
     * constructor for creating the class
     */
    constructor()

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input)

    /**
     * get rest status for the response. Useful override for multi-status response.
     * @return RestStatus for the response
     */
    open fun getStatus(): RestStatus = RestStatus.OK
}
