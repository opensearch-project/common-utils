package org.opensearch.commons.alerting.util

/**
 * Validates that index patterns, wildcards and regex are not used in index names.
 */
object IndexPatternUtils {
    private val PATTERN_SPECIAL_CHARS = setOf(
        '*', // wildcard for any number of characters
        '?', // wildcard for single character
        '+', // one or more quantifier
        '[', // character class start
        ']', // character class end
        '(', // group start
        ')', // group end
        '{', // range quantifier start
        '}', // range quantifier end
        '|', // OR operator
        '\\', // escape character
        '.', // any character
        '^', // start anchor/negation in character class
        '$' // end anchor
    )

    fun containsPatternSyntax(indexName: String): Boolean {
        if (indexName.isEmpty() || indexName == "_all") {
            return true
        }

        // Check for date math expression <...>
        if (indexName.startsWith("<") && indexName.endsWith(">")) {
            return true
        }

        var i = 0
        while (i < indexName.length) {
            when (val currentChar = indexName[i]) {
                '\\' -> i += 2 // Skip escaped character
                in PATTERN_SPECIAL_CHARS -> return true
                else -> i++
            }
        }
        return false
    }
}
