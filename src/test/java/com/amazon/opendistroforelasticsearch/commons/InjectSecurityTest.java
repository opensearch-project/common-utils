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

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.opensearch.commons.ConfigConstants.INJECTED_USER;
import static org.opensearch.commons.ConfigConstants.OPENDISTRO_SECURITY_INJECTED_ROLES;
import static org.opensearch.commons.ConfigConstants.OPENDISTRO_SECURITY_USE_INJECTED_USER_FOR_PLUGINS;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;

public class InjectSecurityTest {

    @Test
    public void testUsersRolesEmpty() {
        ThreadContext tc = new ThreadContext(Settings.EMPTY);
        try (InjectSecurity helper = new InjectSecurity("test-name", Settings.EMPTY, tc)) {
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
            helper.inject("joe", Arrays.asList("ops-role", "logs-role"));
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
        Settings settings = Settings.builder().put(OPENDISTRO_SECURITY_USE_INJECTED_USER_FOR_PLUGINS, true).build();
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
