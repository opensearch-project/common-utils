package org.opensearch.commons.model2.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.commons.model2.ModelSerializer;
import org.opensearch.commons.model2.model.Monitor;

import java.io.IOException;

public class ExecuteMonitorRequest extends ActionRequest {

    private static final Logger LOG = LogManager.getLogger(ExecuteMonitorRequest.class);

    Monitor monitor;

    public ExecuteMonitorRequest(final Monitor monitor) {
        this.monitor = monitor;
    }


    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public void writeTo(final StreamOutput out) throws IOException {
        ModelSerializer.write(out, this.monitor);
    }
}