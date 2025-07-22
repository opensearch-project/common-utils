/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.commons.ConfigConstants.INJECTED_USER;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_INJECTED_ROLES;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USE_INJECTED_USER_FOR_PLUGINS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.commons.authuser.User;

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
    public void testInjectUserInfo() {
        Settings settings = Settings.builder().build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        User user = new User(
            "Bob",
            List.of("backendRole1", "backendRole2"),
            List.of("role1", "role2"),
            Map.of("attr1", "attrValue1", "attr2", "attrValue2"),
            "tenant1"
        );
        try (InjectSecurity helper = new InjectSecurity("test-name", null, threadContext)) {
            helper.injectUserInfo(user);
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNotNull(threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT));
            assertEquals(
                "Bob|backendRole1,backendRole2|role1,role2|tenant1",
                threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
            );
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT));
    }

    @Test
    public void testInjectUserInfoWithPipeInUserName() {
        Settings settings = Settings.builder().build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        User user = new User(
            "Bob|test-pipe",
            List.of("backendRole1", "backendRole2"),
            List.of("role1", "role2"),
            Map.of("attr1", "attrValue1", "attr2", "attrValue2"),
            "tenant1"
        );
        try (InjectSecurity helper = new InjectSecurity("test-name", null, threadContext)) {
            helper.injectUserInfo(user);
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNotNull(threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT));
            assertEquals(
                "Bob\\|test-pipe|backendRole1,backendRole2|role1,role2|tenant1",
                threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT)
            );
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT));
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
            assertTrue(helper.injectProperty("property4", new HashMap<String, String>() {
                {
                    put("key", "value");
                }
            }));
            // verify the set properties are not null and equal to what was set
            assertNull(threadContext.getTransient("property"));
            assertNotNull(threadContext.getTransient("property1"));
            assertEquals(true, threadContext.getTransient("property1"));
            assertNotNull(threadContext.getTransient("property2"));
            assertEquals("some value", threadContext.getTransient("property2"));
            assertNotNull(threadContext.getTransient("property3"));
            assertEquals("", threadContext.getTransient("property3"));
            assertNotNull(threadContext.getTransient("property4"));
            assertEquals(new HashMap<String, String>() {
                {
                    put("key", "value");
                }
            }, threadContext.getTransient("property4"));
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
