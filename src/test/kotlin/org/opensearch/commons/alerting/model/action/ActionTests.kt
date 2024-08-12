package org.opensearch.commons.alerting.model.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.alerting.model.action.Action.Companion.DESTINATION_ID_FIELD
import org.opensearch.commons.alerting.model.action.Action.Companion.ID_FIELD
import org.opensearch.commons.alerting.model.action.Action.Companion.NAME_FIELD
import org.opensearch.commons.alerting.model.action.Action.Companion.THROTTLE_ENABLED_FIELD
import org.opensearch.commons.alerting.randomAction

class ActionTests {

    @Test
    fun `test action asTemplateArgs`() {
        val action = randomAction()

        val templateArgs = action.asTemplateArg()

        assertEquals(
            action.id,
            templateArgs[ID_FIELD],
            "Template arg field 'id' doesn't match"
        )
        assertEquals(
            action.name,
            templateArgs[NAME_FIELD],
            "Template arg field 'name' doesn't match"
        )
        assertEquals(
            action.destinationId,
            templateArgs[DESTINATION_ID_FIELD],
            "Template arg field 'destinationId' doesn't match"
        )
        assertEquals(
            action.throttleEnabled,
            templateArgs[THROTTLE_ENABLED_FIELD],
            "Template arg field 'throttleEnabled' doesn't match"
        )
    }
}
