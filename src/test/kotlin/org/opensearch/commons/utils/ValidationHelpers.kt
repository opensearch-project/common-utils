/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidationHelpers {
    @Test
    fun `test valid names`() {
        /*  Start with letter or underscore
            Followed by letters, numbers, underscore or hyphen
            Total length between 4 and 50 characters */

        assertTrue(isValidName("valid"))
        assertTrue(isValidName("Valid_name"))
        assertTrue(isValidName("valid_name_123"))
        assertTrue(isValidName("_validName"))
        assertTrue(isValidName("valid_name-123"))
        assertTrue(isValidName("_123_valid_name"))

        // Boundary value tests
        assertTrue(isValidName("qwer"))
        assertTrue(isValidName("q".repeat(50)))
        assertTrue(isValidName("____"))
        assertTrue(isValidName("a-b-"))
    }

    @Test
    fun `test invalid names`() {
        // Invalid starting characters
        assertFalse(isValidName("123name"))
        assertFalse(isValidName("-name"))
        assertFalse(isValidName("1name"))

        // Should not have invalid characters
        assertFalse(isValidName("invalid@name"))
        assertFalse(isValidName("invalid name"))
        assertFalse(isValidName("invalid#name"))
        assertFalse(isValidName("invalid.name"))
        assertFalse(isValidName("        "))

        // Should not allow special characters other than _ and -
        assertFalse(isValidName("name!"))
        assertFalse(isValidName("name*"))
        assertFalse(isValidName("name$"))
        assertFalse(isValidName("name%"))

        // Boundary value tests
        assertFalse(isValidName("abc"))
        assertFalse(isValidName("a".repeat(51)))
    }
}
