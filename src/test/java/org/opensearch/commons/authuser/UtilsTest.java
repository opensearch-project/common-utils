/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    public void testEscapePipe_NullInput() {
        assertEquals("", Utils.escapePipe(null));
    }

    @Test
    public void testEscapePipe_EmptyString() {
        assertEquals("", Utils.escapePipe(""));
    }

    @Test
    public void testEscapePipe_NoPipes() {
        assertEquals("normal text", Utils.escapePipe("normal text"));
    }

    @Test
    public void testEscapePipe_SinglePipe() {
        assertEquals("before\\|after", Utils.escapePipe("before|after"));
    }

    @Test
    public void testEscapePipe_MultiplePipes() {
        assertEquals("a\\|b\\|c", Utils.escapePipe("a|b|c"));
    }

    @Test
    public void testEscapePipe_PipeAtStart() {
        assertEquals("\\|text", Utils.escapePipe("|text"));
    }

    @Test
    public void testEscapePipe_PipeAtEnd() {
        assertEquals("text\\|", Utils.escapePipe("text|"));
    }

    @Test
    public void testUnescapePipe_NullInput() {
        assertEquals("", Utils.unescapePipe(null));
    }

    @Test
    public void testUnescapePipe_EmptyString() {
        assertEquals("", Utils.unescapePipe(""));
    }

    @Test
    public void testUnescapePipe_NoEscapedPipes() {
        Assertions.assertEquals("normal text", Utils.unescapePipe("normal text"));
    }

    @Test
    public void testUnescapePipe_SingleEscapedPipe() {
        assertEquals("before|after", Utils.unescapePipe("before\\|after"));
    }

    @Test
    public void testUnescapePipe_MultipleEscapedPipes() {
        assertEquals("a|b|c", Utils.unescapePipe("a\\|b\\|c"));
    }

    @Test
    public void testUnescapePipe_EscapedPipeAtStart() {
        assertEquals("|text", Utils.unescapePipe("\\|text"));
    }

    @Test
    public void testUnescapePipe_EscapedPipeAtEnd() {
        assertEquals("text|", Utils.unescapePipe("text\\|"));
    }

    @Test
    public void testRoundTrip_ComplexString() {
        String original = "user|with|pipes";
        String escaped = Utils.escapePipe(original);
        String unescaped = Utils.unescapePipe(escaped);
        assertEquals(original, unescaped);
    }
}
