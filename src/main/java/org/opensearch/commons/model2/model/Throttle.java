/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.model2.model;

import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.commons.model2.AbstractModel;
import org.opensearch.commons.model2.ToXContentModel;

import java.time.temporal.ChronoUnit;

public class Throttle extends AbstractModel {


    public static NamedXContentRegistry.Entry XCONTENT_REGISTRY = ToXContentModel.createRegistryEntry(Throttle.class);

    public int value;
    public ChronoUnit unit;

    public Throttle() {
        // for serialization
    }

    public Throttle(final int value, final ChronoUnit unit) {
        this.value = value;
        this.unit = unit;
    }

}