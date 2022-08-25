package org.opensearch.commons.model2.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionResponse;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.ToXContentObject;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.commons.model2.ModelSerializer;

import java.io.IOException;

public class ExecuteMonitorResponse extends ActionResponse implements ToXContentObject {

    private static final Logger LOG = LogManager.getLogger(ExecuteMonitorResponse.class);

    public String monitorName;

    public ExecuteMonitorResponse(final String monitorName) {
        this.monitorName = monitorName;
    }

    public ExecuteMonitorResponse(final StreamInput input) throws IOException {
        this.monitorName = input.readString();
    }

    @Override
    public void writeTo(final StreamOutput output) throws IOException {
        ModelSerializer.write(output, this.monitorName);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder xContentBuilder, ToXContent.Params params) throws IOException {
        return ModelSerializer.write(xContentBuilder, this.monitorName);
    }

    @Override
    public boolean isFragment() {
        return false;
    }
}