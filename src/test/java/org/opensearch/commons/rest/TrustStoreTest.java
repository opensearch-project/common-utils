/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.security.KeyStore;

import org.junit.jupiter.api.Test;

public class TrustStoreTest {

    @Test
    public void testCreate() throws Exception {
        String resourceName = "sample.pem";
        String absolutePath = new File(getClass().getClassLoader().getResource(resourceName).getFile()).getAbsolutePath();
        assertTrue(absolutePath.endsWith(File.separator + "sample.pem"));

        KeyStore store = new TrustStore(absolutePath).create();
        assertNotNull(store);
        assertEquals("JKS", store.getType());
    }
}
