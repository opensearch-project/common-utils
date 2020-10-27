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

import static org.elasticsearch.common.xcontent.XContentParserUtils.ensureExpectedToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.internal.ToStringBuilder;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;

/**
 * Gets current Authenticated User - name, odfe roles.
 * If security-plugin is not installed or disabled, it returns empty for user name and roles.
 */
final public class User implements Writeable, ToXContent {

    // field name in toXContent
    public static final String NAME_FIELD = "name";
    public static final String BACKEND_ROLES_FIELD = "backend_roles";
    public static final String ROLES_FIELD = "roles";
    public static final String CUSTOM_ATTRIBUTE_NAMES_FIELD = "custom_attribute_names";

    private final String name;
    private final List<String> backendRoles;
    private final List<String> roles;
    private final List<String> customAttNames;

    public User() {
        name = "";
        backendRoles = new ArrayList<>();
        roles = new ArrayList<>();
        customAttNames = new ArrayList<>();
    }

    public User(final String name, final List<String> backendRoles, List<String> roles, List<String> customAttNames) {
        this.name = name;
        this.backendRoles = backendRoles;
        this.roles = roles;
        this.customAttNames = customAttNames;
    }

    /**
     * Reponse of "GET /_opendistro/_security/authinfo"
     * @param response
     * @throws IOException
     */
    public User(final Response response) throws IOException {
        this(EntityUtils.toString(response.getEntity()));
    }

    @SuppressWarnings("unchecked")
    public User(String json) {
        if (Strings.isNullOrEmpty(json)) {
            throw new IllegalArgumentException("Response json cannot be null");
        }

        Map<String, Object> mapValue = XContentHelper.convertToMap(JsonXContent.jsonXContent, json, false);
        name = (String) mapValue.get("user_name");
        backendRoles = (List<String>) mapValue.get("backend_roles");
        roles = (List<String>) mapValue.get("roles");
        customAttNames = (List<String>) mapValue.get("custom_attribute_names");
    }

    public User(StreamInput in) throws IOException {
        name = in.readString();
        backendRoles = in.readStringList();
        roles = in.readStringList();
        customAttNames = in.readStringList();
    }

    public static User parse(XContentParser parser) throws IOException {
        String name = "";
        List<String> backendRoles = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        List<String> customAttNames = new ArrayList<>();

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser::getTokenLocation);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case NAME_FIELD:
                    name = parser.text();
                    break;
                case BACKEND_ROLES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser::getTokenLocation);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        backendRoles.add(parser.text());
                    }
                    break;
                case ROLES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser::getTokenLocation);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        roles.add(parser.text());
                    }
                    break;
                case CUSTOM_ATTRIBUTE_NAMES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser::getTokenLocation);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        customAttNames.add(parser.text());
                    }
                    break;
                default:
                    break;
            }
        }
        return new User(name, backendRoles, roles, customAttNames);
    }

    /**
     * User String format must be pipe separated as : user_name|backendrole1,backendrole2|roles1,role2
     * @param userString
     * @return
     */
    public static User parse(final String userString) {
        if (Strings.isNullOrEmpty(userString)) {
            return null;
        }

        String[] strs = userString.split("\\|");
        if ((strs.length == 0) || (Strings.isNullOrEmpty(strs[0]))) {
            return null;
        }

        String userName = strs[0].trim();
        List<String> backendRoles = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        if ((strs.length > 1) && !Strings.isNullOrEmpty(strs[1])) {
            backendRoles.addAll(Arrays.asList(strs[1].split(",")));
        }
        if ((strs.length > 2) && !Strings.isNullOrEmpty(strs[2])) {
            roles.addAll(Arrays.asList(strs[2].split(",")));
        }
        return new User(userName, backendRoles, roles, Arrays.asList());
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder
            .startObject()
            .field(NAME_FIELD, name)
            .field(BACKEND_ROLES_FIELD, backendRoles)
            .field(ROLES_FIELD, roles)
            .field(CUSTOM_ATTRIBUTE_NAMES_FIELD, customAttNames);
        return builder.endObject();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(name);
        out.writeStringCollection(backendRoles);
        out.writeStringCollection(roles);
        out.writeStringCollection(customAttNames);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this.getClass());
        builder.add(NAME_FIELD, name);
        builder.add(BACKEND_ROLES_FIELD, backendRoles);
        builder.add(ROLES_FIELD, roles);
        builder.add(CUSTOM_ATTRIBUTE_NAMES_FIELD, customAttNames);
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        User that = (User) obj;
        return this.name.equals(that.name)
            && this.getBackendRoles().equals(that.backendRoles)
            && this.getRoles().equals(that.roles)
            && this.getCustomAttNames().equals(that.customAttNames);
    }

    public String getName() {
        return name;
    }

    public List<String> getBackendRoles() {
        return backendRoles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getCustomAttNames() {
        return customAttNames;
    }
}
