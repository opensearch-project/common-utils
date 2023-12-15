package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.randomQueryLevelMonitor
import org.opensearch.commons.alerting.util.IndexUtils
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MonitorsTest {

    @Test
    fun `test monitor asTemplateArgs`() {
        val monitor = randomQueryLevelMonitor(enabled = true)

        val templateArgs = monitor.asTemplateArg()

        assertEquals(monitor.id, templateArgs[IndexUtils._ID], "Template arg field 'id' doesn't match")
        assertEquals(
            monitor.version, templateArgs[IndexUtils._VERSION], "Template arg field 'version' doesn't match"
        )
        assertEquals(monitor.name, templateArgs[Monitor.NAME_FIELD], "Template arg field 'name' doesn't match")
        assertEquals(
            monitor.enabled, templateArgs[Monitor.ENABLED_FIELD], "Template arg field 'enabled' doesn't match"
        )
        assertEquals(
            monitor.monitorType.toString(), templateArgs[Monitor.MONITOR_TYPE_FIELD],
            "Template arg field 'monitoryType' doesn't match"
        )
        assertEquals(
            monitor.enabledTime?.toEpochMilli(), templateArgs[Monitor.ENABLED_TIME_FIELD],
            "Template arg field 'enabledTime' doesn't match"
        )
        assertEquals(
            monitor.lastUpdateTime.toEpochMilli(), templateArgs[Monitor.LAST_UPDATE_TIME_FIELD],
            "Template arg field 'lastUpdateTime' doesn't match"
        )
        assertNotNull(templateArgs[Monitor.SCHEDULE_FIELD], "Template arg field 'schedule' not set")
        val inputs = templateArgs[Monitor.INPUTS_FIELD] as? List<*>
        assertNotNull(inputs, "Template arg field 'inputs' not set")
        assertEquals(1, inputs.size, "Template arg field 'inputs' is not populated")
    }
}
