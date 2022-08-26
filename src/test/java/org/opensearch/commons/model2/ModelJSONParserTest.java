package org.opensearch.commons.model2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.opensearch.commons.model2.model.Monitor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ModelJSONParserTest {

    private static final Logger LOG = LogManager.getLogger(ModelJSONParserTest.class);

    @Test
    public void testMonitorViaJSON() throws Exception {
        JSONObject json = new JSONObject(new String(ExampleIndexMonitorJSON.exampleIndexMonitor.getBytes()));
        final Monitor monitor = ModelSerializer.read(json, Monitor.class);
        LOG.info(monitor);
        assertNull(monitor.id);
        assertNotNull(monitor.inputs);
        assertNotNull(monitor.inputs.get(0).description);
    }
}
