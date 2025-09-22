/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser;

import static org.opensearch.commons.authuser.Utils.unescapePipe;
import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.opensearch.Version;
import org.opensearch.client.Response;
import org.opensearch.common.Nullable;
import org.opensearch.common.inject.internal.ToStringBuilder;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.commons.ConfigConstants;
import org.opensearch.commons.authuser.util.Base64Helper;
import org.opensearch.core.common.Strings;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.common.io.stream.Writeable;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;

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
    public static final String CUSTOM_ATTRIBUTES_FIELD = "custom_attributes";
    public static final String REQUESTED_TENANT_FIELD = "user_requested_tenant";
    public static final String REQUESTED_TENANT_ACCESS = "user_requested_tenant_access";

    private final String name;
    private final List<String> backendRoles;
    private final List<String> roles;
    private final Map<String, String> customAttributes;
    @Nullable
    private final String requestedTenant;
    @Nullable
    private final String requestedTenantAccess;

    public User() {
        name = "";
        backendRoles = new ArrayList<>();
        roles = new ArrayList<>();
        customAttributes = new HashMap<>();
        requestedTenant = null;
        requestedTenantAccess = null;
    }

    public User(final String name, final List<String> backendRoles, List<String> roles, Map<String, String> customAttributes) {
        this.name = name;
        this.backendRoles = backendRoles;
        this.roles = roles;
        this.customAttributes = customAttributes;
        this.requestedTenant = null;
        this.requestedTenantAccess = null;
    }

    public User(final String name, final List<String> backendRoles, List<String> roles, List<String> customAttNames) {
        this.name = name;
        this.backendRoles = backendRoles;
        this.roles = roles;
        this.customAttributes = this.convertCustomAttributeNamesToMap(customAttNames);
        this.requestedTenant = null;
        this.requestedTenantAccess = null;
    }

    public User(
        final String name,
        final List<String> backendRoles,
        final List<String> roles,
        final Map<String, String> customAttributes,
        @Nullable final String requestedTenant
    ) {
        this.name = name;
        this.backendRoles = backendRoles;
        this.roles = roles;
        this.customAttributes = customAttributes;
        this.requestedTenant = requestedTenant;
        this.requestedTenantAccess = null;
    }

    public User(
        final String name,
        final List<String> backendRoles,
        final List<String> roles,
        final Map<String, String> customAttributes,
        @Nullable final String requestedTenant,
        @Nullable final String requestedTenantAccess
    ) {
        this.name = name;
        this.backendRoles = backendRoles;
        this.roles = roles;
        this.customAttributes = customAttributes;
        this.requestedTenant = requestedTenant;
        this.requestedTenantAccess = requestedTenantAccess;
    }

    /**
     * Reponse of "GET /_opendistro/_security/authinfo"
     * @param response
     * @throws IOException
     */
    public User(final Response response) throws IOException, ParseException {
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

        Map<String, String> customAttributesFromJson = (Map<String, String>) mapValue.get("custom_attributes");
        List<String> customAttNames = (List<String>) mapValue.get("custom_attribute_names");

        if (customAttributesFromJson != null) {
            customAttributes = customAttributesFromJson;
        } else {
            customAttributes = this.convertCustomAttributeNamesToMap(customAttNames);
        }

        requestedTenant = (String) mapValue.getOrDefault("user_requested_tenant", null);
        requestedTenantAccess = (String) mapValue.getOrDefault("user_requested_tenant_access", null);
    }

    public User(StreamInput in) throws IOException {
        name = in.readString();
        backendRoles = in.readStringList();
        roles = in.readStringList();
        if (in.getVersion().onOrAfter(Version.V_3_2_0)) {
            customAttributes = in.readMap(StreamInput::readString, StreamInput::readString);
        } else {
            List<String> customAttNames = in.readStringList();
            customAttributes = this.convertCustomAttributeNamesToMap(customAttNames);
        }
        requestedTenant = in.readOptionalString();
        if (in.getVersion().onOrAfter(Version.V_3_2_0)) {
            requestedTenantAccess = in.readOptionalString();
        } else {
            requestedTenantAccess = null;
        }
    }

    public static User parse(XContentParser parser) throws IOException {
        String name = "";
        List<String> backendRoles = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        Map<String, String> customAttributes = new HashMap<>();
        String requestedTenant = null;
        String requestedTenantAccess = null;

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case NAME_FIELD:
                    name = parser.text();
                    break;
                case BACKEND_ROLES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        backendRoles.add(parser.text());
                    }
                    break;
                case ROLES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        roles.add(parser.text());
                    }
                    break;
                case CUSTOM_ATTRIBUTES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
                    while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                        String attrName = parser.currentName();
                        parser.nextToken();
                        String attrValue = parser.text();
                        customAttributes.put(attrName, attrValue);
                    }
                    break;
                case CUSTOM_ATTRIBUTE_NAMES_FIELD:
                    ensureExpectedToken(XContentParser.Token.START_ARRAY, parser.currentToken(), parser);
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        // Assume custom attribute name values are key=value with no extra "="
                        String[] attrInfo = parser.text().split("=");
                        customAttributes.put(attrInfo[0], attrInfo[1]);
                    }
                    break;
                case REQUESTED_TENANT_FIELD:
                    requestedTenant = parser.textOrNull();
                    break;
                case REQUESTED_TENANT_ACCESS:
                    requestedTenantAccess = parser.textOrNull();
                    break;
                default:
                    break;
            }
        }

        return new User(name, backendRoles, roles, customAttributes, requestedTenant, requestedTenantAccess);
    }

    /**
     * User String format must be pipe separated as : user_name|backendrole1,backendrole2|roles1,role2|tenant|tenantAccess|base64-encoded(serialized(custom atttributes))
     * @param userString
     * @return
     */
    @SuppressWarnings("unchecked")
    public static User parse(final String userString) {
        if (Strings.isNullOrEmpty(userString)) {
            return null;
        }

        // Split on unescaped pipes (negative lookbehind for backslash)
        String[] strs = userString.split("(?<!\\\\)\\|");
        if ((strs.length == 0) || (Strings.isNullOrEmpty(strs[0]))) {
            return null;
        }

        // Unescape the values
        String userName = unescapePipe(strs[0].trim());
        List<String> backendRoles = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        String requestedTenant = null;
        String requestedTenantAccess = null;
        Map<String, String> customAttributes = new HashMap<>();

        if ((strs.length > 1) && !Strings.isNullOrEmpty(strs[1])) {
            backendRoles.addAll(Arrays.stream(strs[1].split(",")).map(Utils::unescapePipe).toList());
        }
        if ((strs.length > 2) && !Strings.isNullOrEmpty(strs[2])) {
            roles.addAll(Arrays.stream(strs[2].split(",")).map(Utils::unescapePipe).toList());
        }
        if ((strs.length > 3) && !Strings.isNullOrEmpty(strs[3])) {
            requestedTenant = unescapePipe(strs[3].trim());
        }
        if ((strs.length > 4) && !Strings.isNullOrEmpty(strs[4])) {
            requestedTenantAccess = strs[4].trim();
        }
        if ((strs.length > 5) && !Strings.isNullOrEmpty(strs[5])) {
            customAttributes = (Map<String, String>) Base64Helper.deserializeObject(strs[5]);
        }

        return new User(userName, backendRoles, roles, customAttributes, requestedTenant, requestedTenantAccess);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder
            .startObject()
            .field(NAME_FIELD, name)
            .field(BACKEND_ROLES_FIELD, backendRoles)
            .field(ROLES_FIELD, roles)
            .field(REQUESTED_TENANT_FIELD, requestedTenant)
            .field(REQUESTED_TENANT_ACCESS, requestedTenantAccess);

        if (customAttributes.size() > 0) {
            builder.field(CUSTOM_ATTRIBUTES_FIELD, customAttributes);
            builder.field(CUSTOM_ATTRIBUTE_NAMES_FIELD, this.getCustomAttributeNamesFromMap(customAttributes));
        } else {
            builder.field(CUSTOM_ATTRIBUTE_NAMES_FIELD, new ArrayList<>());
        }

        return builder.endObject();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(name);
        out.writeStringCollection(backendRoles);
        out.writeStringCollection(roles);
        if (out.getVersion().onOrAfter(Version.V_3_2_0)) {
            out.writeMap(customAttributes, StreamOutput::writeString, StreamOutput::writeString);
        } else {
            List<String> customAttributeNames = new ArrayList<>(customAttributes.keySet());
            out.writeStringCollection(customAttributeNames);
        }
        out.writeOptionalString(requestedTenant);
        if (out.getVersion().onOrAfter(Version.V_3_2_0)) {
            out.writeOptionalString(requestedTenantAccess);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this.getClass());
        builder.add(NAME_FIELD, name);
        builder.add(BACKEND_ROLES_FIELD, backendRoles);
        builder.add(ROLES_FIELD, roles);
        TreeMap<String, String> sortedCustomAttributes = new TreeMap<>();
        sortedCustomAttributes.putAll(customAttributes);
        builder.add(CUSTOM_ATTRIBUTES_FIELD, sortedCustomAttributes);
        builder.add(REQUESTED_TENANT_FIELD, requestedTenant);
        builder.add(REQUESTED_TENANT_ACCESS, requestedTenantAccess);
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User that = (User) obj;
        return this.name.equals(that.name)
            && this.getBackendRoles().equals(that.backendRoles)
            && this.getRoles().equals(that.roles)
            && this.getCustomAttributes().equals(that.customAttributes)
            && (Objects.equals(this.requestedTenant, that.requestedTenant))
            && (Objects.equals(this.requestedTenantAccess, that.requestedTenantAccess));
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

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public List<String> getCustomAttNames() {
        return this.getCustomAttributeNamesFromMap(this.customAttributes);
    }

    @Nullable
    public String getRequestedTenant() {
        return requestedTenant;
    }

    @Nullable
    public String getRequestedTenantAccess() {
        return requestedTenantAccess;
    }

    public boolean isAdminDn(Settings settings) {
        if (settings == null) {
            return false;
        }
        List<String> adminDns = settings.getAsList(ConfigConstants.OPENSEARCH_SECURITY_AUTHCZ_ADMIN_DN, Collections.emptyList());
        return adminDns.contains(this.name);
    }

    private Map<String, String> convertCustomAttributeNamesToMap(List<String> customAttNames) {
        return customAttNames.stream().collect(Collectors.toMap(key -> key, key -> "null"));
    }

    private List<String> getCustomAttributeNamesFromMap(Map<String, String> customAttributes) {
        List<String> customAttNames = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.customAttributes.entrySet()) {
            customAttNames.add(entry.getKey() + "=" + entry.getValue());
        }
        return customAttNames;
    }
}
