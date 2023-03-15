package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.randomDelegate
import org.opensearch.commons.alerting.randomSequence

class CompositeInputTests {
    @Test
    fun `test sequence asTemplateArgs`() {
        val sequence = randomSequence()
        // WHEN
        val templateArgs = sequence.asTemplateArg()

        // THEN
        val templateDelegates = templateArgs
        Assertions.assertEquals(
            templateDelegates[Sequence.DELEGATES_FIELD],
            sequence.delegates,
            "Template args 'id' field does not match:"
        )
    }

    @Test
    fun `test delegate asTemplateArgs`() {
        val delegate = randomDelegate()
        // WHEN
        val templateArgs = delegate.asTemplateArg()

        // THEN
        val templateDelegates = templateArgs
        Assertions.assertEquals(
            templateDelegates[Delegate.ORDER_FIELD],
            delegate.order,
            "Template args 'id' field does not match:"
        )
        Assertions.assertEquals(
            templateDelegates[Delegate.MONITOR_ID_FIELD],
            delegate.monitorId,
            "Template args 'id' field does not match:"
        )
    }

    @Test
    fun `test create Delegate with illegal order value`() {
        try {
            randomDelegate(-1)
            Assertions.fail("Expecting an illegal argument exception")
        } catch (e: IllegalArgumentException) {
            Assertions.assertEquals(
                "Invalid delgate order",
                e.message
            )
        }
    }

    @Test
    fun `test create Delegate with illegal monitorId value`() {
        try {
            randomDelegate(1, "")
            Assertions.fail("Expecting an illegal argument exception")
        } catch (e: IllegalArgumentException) {
            e.message?.let {
                Assertions.assertTrue(
                    it.contains("Invalid characters in id")

                )
            }
        }
    }

    @Test
    fun `test create Chained Findings with illegal monitorId value`() {
        try {
            ChainedMonitorFindings("")
            Assertions.fail("Expecting an illegal argument exception")
        } catch (e: IllegalArgumentException) {
            e.message?.let {
                Assertions.assertTrue(
                    it.contains("Invalid characters in id")

                )
            }
        }
    }
}
