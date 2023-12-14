package org.opensearch.commons.alerting.model;

import org.junit.jupiter.api.Test;
import org.opensearch.commons.alerting.randomBucketLevelTrigger
import kotlin.test.assertEquals

class BucketLevelTriggerTests {

    @Test
    fun `test asTemplateArgs returns expected values`() {
        val bucketLevelTrigger = randomBucketLevelTrigger()
        val templateArg = bucketLevelTrigger.asTemplateArg()
        System.out.println(templateArg)

        assertEquals(templateArg[Trigger.ID_FIELD], bucketLevelTrigger.id)
        assertEquals(templateArg[Trigger.NAME_FIELD], bucketLevelTrigger.name)
        assertEquals(templateArg[Trigger.SEVERITY_FIELD], bucketLevelTrigger.severity)
        assertEquals(templateArg[Trigger.ACTIONS_FIELD], bucketLevelTrigger.actions.map { it.asTemplateArg() })
        assertEquals(templateArg[BucketLevelTrigger.PARENT_BUCKET_PATH], bucketLevelTrigger.bucketSelector.parentBucketPath)
        assertEquals(templateArg[BucketLevelTrigger.CONDITION_FIELD], bucketLevelTrigger.bucketSelector.script.idOrCode)
    }
}
