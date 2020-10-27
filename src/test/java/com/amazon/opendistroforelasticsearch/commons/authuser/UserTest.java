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

import static com.amazon.opendistroforelasticsearch.commons.ConfigConstants.OPENDISTRO_SECURITY_USER_AND_ROLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.junit.Test;

public class UserTest {

    User testUser() {
        return new User("chip", Arrays.asList("admin", "ops"), Arrays.asList("ops_data"), Arrays.asList("attr1", "attr2"));
    }

    @Test
    public void testEmptyConst() {
        User user = new User();
        assertEquals("", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
        assertEquals(0, user.getCustomAttNames().size());
    }

    @Test
    public void testParamsConst() {
        User user = testUser();
        assertFalse(Strings.isNullOrEmpty(user.getName()));
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(1, user.getRoles().size());
        assertEquals(2, user.getCustomAttNames().size());
    }

    @Test
    public void testJsonConst() throws IOException {
        String json =
            "{\"user\":\"User [name=chip, backend_roles=[admin], requestedTenant=null]\",\"user_name\":\"chip\",\"user_requested_tenant\":null,\"remote_address\":\"127.0.0.1:52196\",\"backend_roles\":[\"admin\"],\"custom_attribute_names\":[],\"roles\":[\"alerting_monitor_full\",\"ops_role\",\"own_index\"],\"tenants\":{\"chip\":true},\"principal\":null,\"peer_certificates\":\"0\",\"sso_logout_url\":null}";

        User user = new User(json);
        assertEquals("chip", user.getName());
        assertEquals(1, user.getBackendRoles().size());
        assertEquals(3, user.getRoles().size());
        assertEquals(0, user.getCustomAttNames().size());
    }

    @Test
    public void testStreamConst() throws IOException {
        User user = testUser();
        BytesStreamOutput out = new BytesStreamOutput();
        user.writeTo(out);
        StreamInput in = StreamInput.wrap(out.bytes().toBytesRef().bytes);
        User newUser = new User(in);
        assertEquals("Round tripping User doesn't work", user.toString(), newUser.toString());
        assertEquals("Round tripping User doesn't work", user, newUser);
    }

    @Test
    public void testParseUserString() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient("user_roles_string", "myuser|bckrole1,bckrol2|role1,role2");
        String str = tc.getTransient("user_roles_string");
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("role1"));
        assertTrue(user.getRoles().contains("role2"));
    }

    @Test
    public void testParseUserStringEmpty() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        String str = tc.getTransient(OPENDISTRO_SECURITY_USER_AND_ROLES);
        User user = User.parse(str);
        assertEquals(null, user);
    }

    @Test
    public void testParseUserStringName() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENDISTRO_SECURITY_USER_AND_ROLES, "myuser||");
        String str = tc.getTransient(OPENDISTRO_SECURITY_USER_AND_ROLES);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void testParseUserStringNobackendRoles() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENDISTRO_SECURITY_USER_AND_ROLES, "myuser||role1,role2");
        String str = tc.getTransient(OPENDISTRO_SECURITY_USER_AND_ROLES);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(0, user.getBackendRoles().size());
        assertEquals(2, user.getRoles().size());
    }

    @Test
    public void testParseUserStringNoRoles() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENDISTRO_SECURITY_USER_AND_ROLES, "myuser|brole1,brole2|");
        String str = tc.getTransient(OPENDISTRO_SECURITY_USER_AND_ROLES);
        User user = User.parse(str);

        assertEquals("myuser", user.getName());
        assertEquals(2, user.getBackendRoles().size());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    public void testParseUserStringMalformed() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        tc.putTransient(OPENDISTRO_SECURITY_USER_AND_ROLES, "|backendrole1,backendrole2|role1,role2");
        String str = tc.getTransient(OPENDISTRO_SECURITY_USER_AND_ROLES);
        User user = User.parse(str);
        assertEquals(null, user);
    }
}
