/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opensearch.commons.ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.opensearch.common.Strings;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;

public class UserTest {

    User testNoTenantUser() {
        return new User("chip", Arrays.asList("admin", "ops"), Arrays.asList("ops_data"), Arrays.asList("attr1", "attr2"));
    }

    User testTenantUser() {
        return new User("chip", Arrays.asList("admin", "ops"), Arrays.asList("ops_data"), Arrays.asList("attr1", "attr2"), "__user__");
    }

    @Test
    public void testEmptyConst() {
        User user = new User();
        assertEquals("", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
        assertEquals(0, user.getCustomAttNames().size());
        assertNull(user.getRequestedTenant());
    }

    @Test
    public void testParamsConstForNoTenantUser() {
        User user = testNoTenantUser();
        assertFalse(Strings.isNullOrEmpty(user.getName()));
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(1, user.getRoles().size());
        assertEquals(2, user.getCustomAttNames().size());
        assertNull(user.getRequestedTenant());
    }

    @Test
    public void testParamsConstForTenantUser() {
        User user = testTenantUser();
        assertFalse(Strings.isNullOrEmpty(user.getName()));
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(1, user.getRoles().size());
        assertEquals(2, user.getCustomAttNames().size());
        assertFalse(Strings.isNullOrEmpty(user.getRequestedTenant()));
    }

    @Test
    public void testNullTenantJsonConst() throws IOException {
        String json =
            "{\"user\":\"User [name=chip, backend_roles=[admin], requestedTenant=null]\",\"user_name\":\"chip\",\"user_requested_tenant\":null,\"remote_address\":\"127.0.0.1:52196\",\"backend_roles\":[\"admin\"],\"custom_attribute_names\":[],\"roles\":[\"alerting_monitor_full\",\"ops_role\",\"own_index\"],\"tenants\":{\"chip\":true},\"principal\":null,\"peer_certificates\":\"0\",\"sso_logout_url\":null}";

        User user = new User(json);
        assertEquals("chip", user.getName());
        assertEquals(1, user.getBackendRoles().size());
        assertEquals(3, user.getRoles().size());
        assertEquals(0, user.getCustomAttNames().size());
        assertNull(user.getRequestedTenant());
    }

    @Test
    public void testNonNullTenantJsonConst() throws IOException {
        String json =
            "{\"user\":\"User [name=chip, backend_roles=[admin], requestedTenant=__user__]\",\"user_name\":\"chip\",\"user_requested_tenant\":\"__user__\",\"remote_address\":\"127.0.0.1:52196\",\"backend_roles\":[\"admin\"],\"custom_attribute_names\":[],\"roles\":[\"alerting_monitor_full\",\"ops_role\",\"own_index\"],\"tenants\":{\"chip\":true},\"principal\":null,\"peer_certificates\":\"0\",\"sso_logout_url\":null}";

        User user = new User(json);
        assertEquals("chip", user.getName());
        assertEquals(1, user.getBackendRoles().size());
        assertEquals(3, user.getRoles().size());
        assertEquals(0, user.getCustomAttNames().size());
        assertEquals("__user__", user.getRequestedTenant());
    }

    @Test
    public void testStreamConstForNoTenantUser() throws IOException {
        User user = testNoTenantUser();
        BytesStreamOutput out = new BytesStreamOutput();
        user.writeTo(out);
        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        User newUser = new User(in);
        assertEquals(user.toString(), newUser.toString(), "Round tripping User doesn't work");
        assertEquals(user, newUser, "Round tripping User doesn't work");
    }

    @Test
    public void testStreamConstForTenantUser() throws IOException {
        User user = testTenantUser();
        BytesStreamOutput out = new BytesStreamOutput();
        user.writeTo(out);
        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        User newUser = new User(in);
        assertEquals(user.toString(), newUser.toString(), "Round tripping User doesn't work");
        assertEquals(user, newUser, "Round tripping User doesn't work");
    }

    @Test
    public void testParseUserString() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser|bckrole1,bckrol2|role1,role2|myTenant");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
        assertEquals("myTenant", user.getRequestedTenant());
    }

    @Test
    public void testParseUserStringEmpty() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);
        assertNull(user);
    }

    @Test
    public void testParseUserStringName() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser||");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void testParseUserStringNameWithTenant() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser|||myTenant");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
        assertEquals("myTenant", user.getRequestedTenant());
    }

    @Test
    public void testParseUserStringNobackendRoles() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser||role1,role2");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(2, user.getRoles().size());
    }

    @Test
    public void testParseUserStringNoRoles() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser|brole1,brole2|");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void testParseUserStringNoRolesWithTenant() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "myuser|brole1,brole2||myTenant");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
        assertEquals("myTenant", user.getRequestedTenant());
    }

    @Test
    public void testParseUserStringMalformed() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT, "|backendrole1,backendrole2|role1,role2");
        String str = tc.getTransient(OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT);
        User user = User.parse(str);
        assertNull(user);
    }
}
