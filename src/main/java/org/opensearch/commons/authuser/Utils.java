/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.authuser;

public final class Utils {

    // Helper method to escape pipe characters
    public static String escapePipe(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("|", "\\|");
    }

    // Helper method to un-escape pipe characters
    public static String unescapePipe(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\|", "|");
    }
}
