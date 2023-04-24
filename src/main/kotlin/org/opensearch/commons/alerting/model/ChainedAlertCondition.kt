package org.opensearch.commons.alerting.model

import org.opensearch.common.UUIDs
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.script.Script
import java.io.IOException

data class ChainedAlertCondition(
    val id: String,
    val name: String,
    val severity: String,
    val actions: List<Action>,
    val condition: String,
) : BaseModel {

    companion object {
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val SEVERITY_FIELD = "severity"
        const val ACTIONS_FIELD = "actions"
        const val CONDITION_FIELD = "condition"

        @Throws(IOException::class)
        fun parse(xcp: XContentParser): ChainedAlertCondition {
            var id = UUIDs.base64UUID() // assign a default id if one is not specified
            lateinit var name: String
            lateinit var severity: String
            lateinit var condition: String
            val actions: MutableList<Action> = mutableListOf()

            if (xcp.currentToken() != XContentParser.Token.START_OBJECT && xcp.currentToken() != XContentParser.Token.FIELD_NAME) {
                XContentParserUtils.throwUnknownToken(xcp.currentToken(), xcp.tokenLocation)
            }

            // If the parser began on START_OBJECT, move to the next token so that the while loop enters on
            // the fieldName (or END_OBJECT if it's empty).
            if (xcp.currentToken() == XContentParser.Token.START_OBJECT) xcp.nextToken()

            while (xcp.currentToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()

                xcp.nextToken()
                when (fieldName) {
                    ID_FIELD -> id = xcp.text()
                    NAME_FIELD -> name = xcp.text()
                    SEVERITY_FIELD -> severity = xcp.text()
                    CONDITION_FIELD -> {
                        xcp.nextToken()
                        condition = xcp.text()
                        xcp.nextToken()
                    }
                    ACTIONS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actions.add(Action.parse(xcp))
                        }
                    }
                }
                xcp.nextToken()
            }


            return ChainedAlertCondition(id, name, severity, actions, condition)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): ChainedAlertCondition {
            return ChainedAlertCondition(sin)
        }
    }

    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeString(severity)
        out.writeCollection(actions)
       out.writeString(condition)
    }

    override fun toXContent(builder: XContentBuilder, p1: ToXContent.Params?): XContentBuilder {
        builder.startObject()
            .startObject()
            .field(ID_FIELD, id)
            .field(NAME_FIELD, name)
            .field(SEVERITY_FIELD, severity)
            .startObject(CONDITION_FIELD)
            .field(CONDITION_FIELD, condition)
            .endObject()
            .field(ACTIONS_FIELD, actions.toTypedArray())
            .endObject()
            .endObject()
        return builder
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readString(), // name
        sin.readString(), // severity
        sin.readList(::Action), // actions
        sin.readString() // condition
    )
}


