package org.opensearch.commons.model2.model;

import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.commons.model2.AbstractModel;
import org.opensearch.commons.model2.ToXContentModel;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class Schedule extends AbstractModel {

    public static NamedXContentRegistry.Entry XCONTENT_REGISTRY = ToXContentModel.createRegistryEntry(Schedule.class);

    public String expression;
    public ZoneId timezone;
    public Integer interval;
    public ChronoUnit unit;
    public String type;

    public Schedule() {
        // for serialization
    }

    public Schedule(final String expression, final ZoneId timezone, final Integer interval, final ChronoUnit unit, final String type) {
        this.expression = expression;
        this.timezone = timezone;
        this.interval = interval;
        this.unit = unit;
        this.type = type;
    }
}