package org.opensearch.commons.alerting

import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.Trigger
import org.opensearch.test.OpenSearchTestCase
import java.time.Instant
import kotlin.test.assertEquals

internal class MonitorTests {
    @Test
    fun `test enabled time`() {
        val monitor = randomQueryLevelMonitor()
        val enabledMonitor = monitor.copy(enabled = true, enabledTime = Instant.now())
        try {
            enabledMonitor.copy(enabled = false)
            OpenSearchTestCase.fail("Disabling monitor with enabled time set should fail.")
        } catch (e: IllegalArgumentException) {
        }

        val disabledMonitor = monitor.copy(enabled = false, enabledTime = null)

        try {
            disabledMonitor.copy(enabled = true)
            OpenSearchTestCase.fail("Enabling monitor without enabled time should fail")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun `test monitor allows more than 10 triggers`() {
        val monitor = randomQueryLevelMonitor()

        val manyTriggers = mutableListOf<Trigger>()
        var i = 0
        while (i <= 10) {
            manyTriggers.add(randomQueryLevelTrigger())
            ++i
        }

        val monitorWithManyTriggers = monitor.copy(triggers = manyTriggers)
        assertEquals(11, monitorWithManyTriggers.triggers.size)
    }
}
