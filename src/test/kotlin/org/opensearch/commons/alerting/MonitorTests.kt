package org.opensearch.commons.alerting

import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.Trigger
import org.opensearch.test.OpenSearchTestCase
import java.lang.IllegalArgumentException
import java.time.Instant

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
    fun `test max triggers`() {
        val monitor = randomQueryLevelMonitor()

        val tooManyTriggers = mutableListOf<Trigger>()
        var i = 0
        while (i <= 10) {
            tooManyTriggers.add(randomQueryLevelTrigger())
            ++i
        }

        try {
            monitor.copy(triggers = tooManyTriggers)
            OpenSearchTestCase.fail("Monitor with too many triggers should be rejected.")
        } catch (e: IllegalArgumentException) {
        }
    }
}
