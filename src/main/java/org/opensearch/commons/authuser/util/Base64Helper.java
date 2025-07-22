/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser.util;

import static org.opensearch.commons.authuser.util.SafeSerializationUtils.isSafeClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import org.opensearch.OpenSearchException;
import org.opensearch.core.common.Strings;

/**
 * Provides support for Serialization/Deserialization of objects of supported classes into/from Base64 encoded stream
 * using JDK's in-built serialization protocol implemented by the ObjectOutputStream and ObjectInputStream classes.
 */
public class Base64Helper {

    private final static class SafeObjectOutputStream extends ObjectOutputStream {

        static ObjectOutputStream create(ByteArrayOutputStream out) throws IOException {
            return new Base64Helper.SafeObjectOutputStream(out);
        }

        private SafeObjectOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            Class<?> clazz = obj.getClass();
            if (isSafeClass(clazz)) {
                return obj;
            }
            throw new IOException("Unauthorized serialization attempt " + clazz.getName());
        }
    }

    public static String serializeObject(final Serializable object) {

        assert object != null;

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final ObjectOutputStream out = Base64Helper.SafeObjectOutputStream.create(bos)) {
            out.writeObject(object);
        } catch (final Exception e) {
            throw new OpenSearchException("Instance {} of class {} is not serializable", e, object, object.getClass());
        }
        final byte[] bytes = bos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Serializable deserializeObject(final String string) {

        assert !Strings.isNullOrEmpty(string);

        final byte[] bytes = Base64.getDecoder().decode(string);
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (Base64Helper.SafeObjectInputStream in = new Base64Helper.SafeObjectInputStream(bis)) {
            return (Serializable) in.readObject();
        } catch (final Exception e) {
            throw new OpenSearchException(e);
        }
    }

    private final static class SafeObjectInputStream extends ObjectInputStream {
        public SafeObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            Class<?> clazz = super.resolveClass(desc);

            if (isSafeClass(clazz)) {
                return clazz;
            }

            throw new InvalidClassException("Unauthorized deserialization attempt ", clazz.getName());
        }
    }
}
