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

package org.opensearch.commons.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Helper class to read raw pem files to keystore.
 */
public class TrustStore {

    private final String effectiveKeyAlias = "al";
    private final String storeType = "JKS";
    private final String certType = "X.509";
    private final String cert;

    public TrustStore(final String file) {
        cert = file;
    }

    public KeyStore create() throws IOException, GeneralSecurityException {
        X509Certificate[] trustCerts = loadCertificatesFromFile(cert);
        return toTrustStore(effectiveKeyAlias, trustCerts);
    }

    private X509Certificate[] loadCertificatesFromFile(String file) throws IOException, GeneralSecurityException {
        if (file == null) {
            return null;
        }
        CertificateFactory fact = CertificateFactory.getInstance(certType);
        try (FileInputStream is = new FileInputStream(file)) {
            Collection<? extends Certificate> certs = fact.generateCertificates(is);
            X509Certificate[] x509Certs = new X509Certificate[certs.size()];
            int i = 0;
            for (Certificate cert : certs) {
                x509Certs[i++] = (X509Certificate) cert;
            }
            return x509Certs;
        }
    }

    private KeyStore toTrustStore(final String trustCertificatesAliasPrefix, final X509Certificate[] trustCertificates) throws IOException,
        GeneralSecurityException {
        if (trustCertificates == null) {
            return null;
        }
        KeyStore ks = KeyStore.getInstance(storeType);
        ks.load(null);

        if (trustCertificates != null) {
            for (int i = 0; i < trustCertificates.length; i++) {
                X509Certificate x509Certificate = trustCertificates[i];
                ks.setCertificateEntry(trustCertificatesAliasPrefix + "_" + i, x509Certificate);
            }
        }
        return ks;
    }
}
