/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Provides functionality to verify if a class is categorised to be safe for serialization or
 * deserialization by the security plugin.
 * <br/>
 * All methods are package private.
 */
public final class SafeSerializationUtils {

    private static final Set<Class<?>> SAFE_CLASSES = Set.of(String.class, SocketAddress.class, InetSocketAddress.class, Pattern.class);

    private static final Set<Class<?>> SAFE_ASSIGNABLE_FROM_CLASSES = Set
        .of(InetAddress.class, Number.class, Collection.class, Map.class, Enum.class);

    private static final Set<String> SAFE_CLASS_NAMES = Set
        .of(
            "org.ldaptive.LdapAttribute$LdapAttributeValues",
            "com.google.common.collect.ImmutableMap$SerializedForm",
            "com.google.common.collect.ImmutableBiMap$SerializedForm"
        );
    static final Map<Class<?>, Boolean> safeClassCache = new ConcurrentHashMap<>();

    static boolean isSafeClass(Class<?> cls) {
        return safeClassCache.computeIfAbsent(cls, SafeSerializationUtils::computeIsSafeClass);
    }

    static boolean computeIsSafeClass(Class<?> cls) {
        return cls.isArray() || SAFE_CLASSES.contains(cls) || SAFE_CLASS_NAMES.contains(cls.getName()) || isAssignableFromSafeClass(cls);
    }

    private static boolean isAssignableFromSafeClass(Class<?> cls) {
        for (Class<?> safeClass : SAFE_ASSIGNABLE_FROM_CLASSES) {
            if (safeClass.isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }

    static void prohibitUnsafeClasses(Class<?> clazz) throws IOException {
        if (!isSafeClass(clazz)) {
            throw new IOException("Unauthorized serialization attempt " + clazz.getName());
        }
    }
}
