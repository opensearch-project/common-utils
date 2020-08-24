package com.amazon.opendistroforelasticsearch.commons;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.junit.Assert;
import org.junit.Test;


import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.INJECTED_USER;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_INJECTED_ROLES;
import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_USE_INJECTED_USER_DEFAULT;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class InjectSecurityTest {

    @Test
    public void testUsersRolesEmpty() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        try(InjectSecurity helper = new InjectSecurity("test-name", Settings.EMPTY,tc)) {
            helper.inject("", null);
        }
        Assert.assertNull(tc.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES));
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
            helper.inject("joe", Arrays.asList("ops-role","logs-role"));
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNotNull(threadContext.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES));
            assertEquals("plugin|ops-role,logs-role", threadContext.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES));
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES));
    }

    @Test
    public void testInjectUser() {
        Settings settings = Settings.builder()
                .put(OPENDISTRO_SECURITY_USE_INJECTED_USER_DEFAULT, true)
                .build();
        Settings headerSettings = Settings.builder().put("request.headers.default", "1").build();
        ThreadContext threadContext = new ThreadContext(headerSettings);
        threadContext.putHeader("name", "opendistro");
        threadContext.putTransient("ctx.name", "plugin");

        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));

        try (InjectSecurity helper = new InjectSecurity("test-name", settings, threadContext)) {
            helper.inject("joe", Arrays.asList("ops-role","logs-role"));
            assertEquals("1", threadContext.getHeader("default"));
            assertEquals("opendistro", threadContext.getHeader("name"));
            assertEquals("plugin", threadContext.getTransient("ctx.name"));
            assertNull(threadContext.getTransient(OPENDISTRO_SECURITY_INJECTED_ROLES));
            assertNotNull(threadContext.getTransient(INJECTED_USER));
            assertEquals("joe", threadContext.getTransient(INJECTED_USER));
        }
        assertEquals("1", threadContext.getHeader("default"));
        assertEquals("opendistro", threadContext.getHeader("name"));
        assertEquals("plugin", threadContext.getTransient("ctx.name"));
        assertNull(threadContext.getTransient(INJECTED_USER));
    }
}
