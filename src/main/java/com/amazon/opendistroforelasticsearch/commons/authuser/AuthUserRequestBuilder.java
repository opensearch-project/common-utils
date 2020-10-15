/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.commons.authuser;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;

import com.amazon.opendistroforelasticsearch.commons.ConfigConstants;

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
