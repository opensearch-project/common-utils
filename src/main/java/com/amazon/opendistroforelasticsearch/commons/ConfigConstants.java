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

package com.amazon.opendistroforelasticsearch.commons;

public class ConfigConstants {

    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HOST_DEFAULT = "localhost";
    public static final String HTTP_PORT = "http.port";
    public static final int HTTP_PORT_DEFAULT = 9200;
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_DEFAULT = "application/json";
    public static final String AUTHORIZATION = "Authorization";

    // These reside in security plugin.
    public static final String OPENDISTRO_SECURITY_SSL_HTTP_PEMCERT_FILEPATH = "opendistro_security.ssl.http.pemcert_filepath";
    public static final String OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH = "opendistro_security.ssl.http.keystore_filepath";
    public static final String OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD = "opendistro_security.ssl.http.keystore_password";
    public static final String OPENDISTRO_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD = "opendistro_security.ssl.http.keystore_keypassword";
    public static final String OPENDISTRO_SECURITY_INJECTED_ROLES = "opendistro_security_injected_roles";
    public static final String INJECTED_USER = "injected_user";
    public static final String OPENDISTRO_SECURITY_USE_INJECTED_USER_FOR_PLUGINS = "opendistro_security_use_injected_user_for_plugins";
    public static final String OPENDISTRO_SECURITY_SSL_HTTP_ENABLED = "opendistro_security.ssl.http.enabled";
}
