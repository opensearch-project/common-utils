/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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

package org.opensearch.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.commons.ConfigConstants.INJECTED_USER;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_INJECTED_ROLES;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;

public class InjectSecurityTest {

    @Test
    public void testUsersRolesEmpty() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        try (InjectSecurity helper = new InjectSecurity("test-name", Settings.EMPTY, tc)) {
            helper.inject("", null);
        }
        assertNull(tc.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
    }

    @Test
    public void testInjectRoles() {
        Settings settings = Settings.builder().build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        try (InjectSecurity helper = new InjectSecurity("test-name", settings, threadContext)) {
            helper.inject("joe", Arrays.asList("ops-role", "logs-role"));
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNotNull(threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
            assertEquals("plugin|ops-role,logs-role", threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
    }

    @Test
    public void testInjectUser() {
        Settings settings = Settings.builder().put(OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS, true).build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        try (InjectSecurity helper = new InjectSecurity("test-name", settings, threadContext)) {
            helper.inject("joe", Arrays.asList("ops-role", "logs-role"));
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNull(threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
            assertNotNull(threadContext.getTransient(INJECTED_USER));
            assertEquals("joe", threadContext.getTransient(INJECTED_USER));
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(INJECTED_USER));
    }

    @Test
    public void testInjectProperty() {
        Settings settings = Settings.builder().put(OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS, false).build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        try (InjectSecurity helper = new InjectSecurity("test-name", settings, threadContext)) {
            helper.inject("joe", Arrays.asList("ops-role", "logs-role"));
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNotNull(threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
            // cannot inject property that is already set
            assertFalse(helper.injectProperty(OPENSEARCH_SECURITY_INJECTED_ROLES, "new value"));
            assertEquals("plugin|ops-role,logs-role", threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
            // cannot inject invalid property/value
            assertFalse(helper.injectProperty("", "new value"));
            assertFalse(helper.injectProperty(null, "new value"));
            assertFalse(helper.injectProperty("property", null));
            // can inject non-set valid properties
            assertTrue(helper.injectProperty("property1", true));
            assertTrue(helper.injectProperty("property2", "some value"));
            assertTrue(helper.injectProperty("property3", ""));
            assertTrue(helper.injectProperty("property4", Map.of("key", "value")));
            // verify the set properties are not null and equal to what was set
            assertNull(threadContext.getTransient("property"));
            assertNotNull(threadContext.getTransient("property1"));
            assertEquals(true, threadContext.getTransient("property1"));
            assertNotNull(threadContext.getTransient("property2"));
            assertEquals("some value", threadContext.getTransient("property2"));
            assertNotNull(threadContext.getTransient("property3"));
            assertEquals("", threadContext.getTransient("property3"));
            assertNotNull(threadContext.getTransient("property4"));
            assertEquals(Map.of("key", "value"), threadContext.getTransient("property4"));
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(OPENSEARCH_SECURITY_INJECTED_ROLES));
        assertNull(threadContext.getTransient("property1"));
        assertNull(threadContext.getTransient("property2"));
        assertNull(threadContext.getTransient("property3"));
        assertNull(threadContext.getTransient("property4"));
    }
}
