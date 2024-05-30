package org.opensearch.commons.alerting.model.remote.monitors

import org.opensearch.common.CheckedFunction
import org.opensearch.common.UUIDs
import org.opensearch.commons.alerting.model.Trigger
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.core.ParseField
import org.opensearch.core.common.bytes.BytesReference
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException
import java.nio.ByteBuffer

data class RemoteMonitorTrigger(
    override val id: String,
    override val name: String,
    override val severity: String,
    override val actions: List<Action>,
    val trigger: BytesReference
) : Trigger {
    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(),
        sin.readString(),
        sin.readString(),
        sin.readList(::Action),
        sin.readBytesReference()
    )

    fun asTemplateArg(): Map<String, Any?> {
        val bytes = trigger.toBytesRef().bytes
        return mapOf(
            Trigger.ID_FIELD to id,
            Trigger.NAME_FIELD to name,
            Trigger.SEVERITY_FIELD to severity,
            Trigger.ACTIONS_FIELD to actions.map { it.asTemplateArg() },
            TRIGGER_SIZE to bytes.size,
            TRIGGER_FIELD to bytes
        )
    }

    override fun name(): String {
        return REMOTE_MONITOR_TRIGGER_FIELD
    }

    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeString(name)
        out.writeString(severity)
        out.writeCollection(actions)
        out.writeBytesReference(trigger)
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        val bytes = trigger.toBytesRef().bytes
        return builder.startObject()
            .startObject(REMOTE_MONITOR_TRIGGER_FIELD)
            .field(Trigger.ID_FIELD, id)
            .field(Trigger.NAME_FIELD, name)
            .field(Trigger.SEVERITY_FIELD, severity)
            .field(Trigger.ACTIONS_FIELD, actions.toTypedArray())
            .field(TRIGGER_SIZE, bytes.size)
            .field(TRIGGER_FIELD, bytes)
            .endObject()
            .endObject()
    }

    companion object {
        const val TRIGGER_FIELD = "trigger"
        const val TRIGGER_SIZE = "size"
        const val REMOTE_MONITOR_TRIGGER_FIELD = "remote_monitor_trigger"

        val XCONTENT_REGISTRY = NamedXContentRegistry.Entry(
            Trigger::class.java,
            ParseField(REMOTE_MONITOR_TRIGGER_FIELD),
            CheckedFunction { parseInner(it) }
        )

        fun parseInner(xcp: XContentParser): RemoteMonitorTrigger {
            var id = UUIDs.base64UUID() // assign a default triggerId if one is not specified
            lateinit var name: String
            lateinit var severity: String
            val actions: MutableList<Action> = mutableListOf()
            var bytes: ByteArray? = null
            var size: Int = 0

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
                    Trigger.ID_FIELD -> id = xcp.text()
                    Trigger.NAME_FIELD -> name = xcp.text()
                    Trigger.SEVERITY_FIELD -> severity = xcp.text()
                    Trigger.ACTIONS_FIELD -> {
                        XContentParserUtils.ensureExpectedToken(
                            XContentParser.Token.START_ARRAY,
                            xcp.currentToken(),
                            xcp
                        )
                        while (xcp.nextToken() != XContentParser.Token.END_ARRAY) {
                            actions.add(Action.parse(xcp))
                        }
                    }
                    TRIGGER_FIELD -> bytes = xcp.binaryValue()
                    TRIGGER_SIZE -> size = xcp.intValue()
                }
                xcp.nextToken()
            }
            val trigger = BytesReference.fromByteBuffer(ByteBuffer.wrap(bytes, 0, size))
            return RemoteMonitorTrigger(id, name, severity, actions, trigger)
        }
    }
}
