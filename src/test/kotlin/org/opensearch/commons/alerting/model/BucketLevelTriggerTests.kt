package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.BucketLevelTrigger.Companion.CONDITION_FIELD
import org.opensearch.commons.alerting.model.BucketLevelTrigger.Companion.LANG_FIELD
import org.opensearch.commons.alerting.model.BucketLevelTrigger.Companion.PARENT_BUCKET_PATH
import org.opensearch.commons.alerting.model.BucketLevelTrigger.Companion.SCRIPT_FIELD
import org.opensearch.commons.alerting.model.BucketLevelTrigger.Companion.SOURCE_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.ACTIONS_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.ID_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.Trigger.Companion.SEVERITY_FIELD
import org.opensearch.commons.alerting.randomBucketLevelTrigger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BucketLevelTriggerTests {

    @Test
    fun `test BucketLevelTrigger asTemplateArgs`() {
        val trigger = randomBucketLevelTrigger()

        val templateArgs = trigger.asTemplateArg()

        assertEquals(trigger.id, templateArgs[ID_FIELD], "Template arg field 'id' doesn't match")
        assertEquals(trigger.name, templateArgs[NAME_FIELD], "Template arg field 'name' doesn't match")
        assertEquals(trigger.severity, templateArgs[SEVERITY_FIELD], "Template arg field 'severity' doesn't match")
        val actions = templateArgs[ACTIONS_FIELD] as List<*>
        assertEquals(
            trigger.actions.size,
            actions.size,
            "Template arg field 'actions' doesn't match"
        )
        assertEquals(
            trigger.getParentBucketPath(),
            templateArgs[PARENT_BUCKET_PATH],
            "Template arg field 'parentBucketPath' doesn't match"
        )
        val condition = templateArgs[CONDITION_FIELD] as? Map<*, *>
        assertNotNull(condition, "Template arg field 'condition' is empty")
        val script = condition[SCRIPT_FIELD] as? Map<*, *>
        assertNotNull(script, "Template arg field 'condition.script' is empty")
        assertEquals(
            trigger.bucketSelector.script.idOrCode,
            script[SOURCE_FIELD],
            "Template arg field 'script.source' doesn't match"
        )
        assertEquals(
            trigger.bucketSelector.script.lang,
            script[LANG_FIELD],
            "Template arg field 'script.lang' doesn't match"
        )
    }
}
