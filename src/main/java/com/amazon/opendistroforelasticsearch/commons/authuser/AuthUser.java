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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import com.amazon.opendistroforelasticsearch.commons.ConfigConstants;

/**
 * Gets current Authenticated User - name, odfe roles.
 * If security-plugin is not installed or disabled, it returns null for user name and roles.
 */
final public class AuthUser {
    private String userName = "";
    private String userRequestedTenant = "";
    private String remoteAddress = "";
    private ArrayList<String> backendRoles = new ArrayList<>();
    private ArrayList<String> roles = new ArrayList<>();

    private final Settings settings;
    private final RestClient restClient;
    private final List<String> authTokens;

    private final Logger log = LogManager.getLogger(this.getClass());

    public AuthUser(final Settings settings, final RestClient restClient, final List<String> authTokens) {
        this.settings = checkNotNull(settings, "Cluster settings cannot be null");
        this.restClient = checkNotNull(restClient, "Rest client cannot be null");
        this.authTokens = checkNotNull(authTokens, "Auth token cannot be null");
    }

    /**
     * todo: this method needs to made more robust, mainly around handling auth header.
     * @return
     * @throws IOException
     */
    public final AuthUser get() throws IOException {
        if (authTokens.size() == 0) {
            log.debug("Auth token is not present.");
            return this;
        }

        Request request = new Request("GET", "/_opendistro/_security/authinfo");
        request
            .setOptions(
                RequestOptions.DEFAULT
                    .toBuilder()
                    .addHeader(ConfigConstants.CONTENT_TYPE, ConfigConstants.CONTENT_TYPE_DEFAULT)
                    .addHeader(ConfigConstants.AUTHORIZATION, authTokens.get(0))
            );

        Response response = restClient.performRequest(request);
        parse(EntityUtils.toString(response.getEntity()));
        return this;
    }

    @SuppressWarnings("unchecked")
    private void parse(String json) {
        Map<String, Object> mapValue = XContentHelper.convertToMap(JsonXContent.jsonXContent, json, false);
        userName = (String) mapValue.get("user_name");
        userRequestedTenant = (String) mapValue.get("user_requested_tenant");
        remoteAddress = (String) mapValue.get("remote_address");
        backendRoles = (ArrayList<String>) mapValue.get("backend_roles");
        roles = (ArrayList<String>) mapValue.get("roles");
    }

    public String getUserName() {
        return userName;
    }

    public String getRolesString() {
        return (roles.size() == 0) ? "" : String.join(",", roles);
    }

    public String getUserRequestedTenant() {
        return userRequestedTenant;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public List<String> getBackendRoles() {
        return backendRoles;
    }

    public List<String> getRoles() {
        return roles;
    }
}
