/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.destination.util;

import java.util.regex.Pattern;

import org.opensearch.common.Strings;
import org.opensearch.common.ValidationException;

public class Util {
    private Util() {}

    public static final Pattern SNS_ARN_REGEX = Pattern
        .compile("^arn:aws(-[^:]+)?:sns:([a-zA-Z0-9-]+):([0-9]{12}):([a-zA-Z0-9-_]+)(\\.fifo)?$");
    public static final Pattern IAM_ARN_REGEX = Pattern.compile("^arn:aws(-[^:]+)?:iam::([0-9]{12}):([a-zA-Z0-9-/_+=@.,]+)$");

    public static String getRegion(String arn) {
        // sample topic arn arn:aws:sns:us-west-2:075315751589:test-notification
        if (isValidSNSArn(arn)) {
            return arn.split(":")[3];
        }
        throw new IllegalArgumentException("Unable to retrieve region from ARN " + arn);
    }

    public static boolean isValidIAMArn(String arn) {
        return Strings.hasLength(arn) && IAM_ARN_REGEX.matcher(arn).find();
    }

    public static boolean isValidSNSArn(String arn) throws ValidationException {
        return Strings.hasLength(arn) && SNS_ARN_REGEX.matcher(arn).find();
    }
}
