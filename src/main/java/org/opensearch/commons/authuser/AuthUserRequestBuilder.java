/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser;

import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.common.Strings;
import org.opensearch.commons.ConfigConstants;

public class AuthUserRequestBuilder {
    private final String auth;

    public AuthUserRequestBuilder(String auth) {
        if (Strings.isNullOrEmpty(auth)) {
            throw new IllegalArgumentException("Authorization token cannot be null");
        }
        this.auth = auth;
    }

    public Request build() {
        Request request = new Request("GET", "/_opendistro/_security/authinfo");
        request
            .setOptions(
                RequestOptions.DEFAULT
                    .toBuilder()
                    .addHeader(ConfigConstants.CONTENT_TYPE, ConfigConstants.CONTENT_TYPE_DEFAULT)
                    .addHeader(ConfigConstants.AUTHORIZATION, auth)
            );
        return request;
    }
}
