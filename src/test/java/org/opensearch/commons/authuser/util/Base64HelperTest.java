/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opensearch.OpenSearchException;

public class Base64HelperTest {

    private static final class NotSafeSerializable implements Serializable {
        private static final long serialVersionUID = 5135559266828470092L;
    }

    private static Serializable ds(Serializable s) {
        return Base64Helper.deserializeObject(Base64Helper.serializeObject(s));
    }

    @Test
    public void testString() {
        String string = "string";
        assertThat(ds(string), is(string));
    }

    @Test
    public void testInteger() {
        Integer integer = 0;
        assertThat(ds(integer), is(integer));
    }

    @Test
    public void testDouble() {
        Double number = 0.0;
        assertThat(ds(number), is(number));
    }

    @Test
    public void testInetSocketAddress() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
        assertThat(ds(inetSocketAddress), is(inetSocketAddress));
    }

    @Test
    public void testHashMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertThat(ds(map), is(map));
    }

    @Test
    public void testArrayList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("value");
        assertThat(ds(list), is(list));
    }

    @Test
    public void notSafeSerializable() {
        final OpenSearchException exception = assertThrows(
            OpenSearchException.class,
            () -> Base64Helper.serializeObject(new NotSafeSerializable())
        );
        assertThat(exception.getMessage(), containsString("NotSafeSerializable is not serializable"));
    }

    @Test
    public void notSafeDeserializable() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(new NotSafeSerializable());
        }
        Base64.Encoder encoder = Base64.getEncoder();
        final OpenSearchException exception = assertThrows(
            OpenSearchException.class,
            () -> Base64Helper.deserializeObject(new String(encoder.encode(bos.toByteArray())))
        );
        assertThat(exception.getMessage(), containsString("Unauthorized deserialization attempt"));
    }

    /**
     * Just one sanity test comprising invocation of JDK and Custom Serialization.
     *
     * Individual scenarios are covered by Base64CustomHelperTest and Base64JDKHelperTest
     */
    @Test
    public void testSerde() {
        String test = "string";
        assertThat(ds(test), is(test));
    }

    @Test
    public void testDuplicatedItemSizes() {
        var largeObject = new HashMap<String, Object>();
        var hm = new HashMap<>();
        IntStream.range(0, 100).forEach(i -> { hm.put("c" + i, "cvalue" + i); });
        IntStream.range(0, 100).forEach(i -> { largeObject.put("b" + i, hm); });

        final var jdkSerialized = Base64Helper.serializeObject(largeObject);

        assertThat(jdkSerialized.length(), is(3832));
    }
}
