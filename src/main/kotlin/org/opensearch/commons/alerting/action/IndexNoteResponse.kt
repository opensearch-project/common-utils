package org.opensearch.commons.alerting.action

import org.opensearch.commons.alerting.model.Note
import org.opensearch.commons.alerting.util.IndexUtils.Companion._ID
import org.opensearch.commons.alerting.util.IndexUtils.Companion._PRIMARY_TERM
import org.opensearch.commons.alerting.util.IndexUtils.Companion._SEQ_NO
import org.opensearch.commons.notifications.action.BaseResponse
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import java.io.IOException

class IndexNoteResponse : BaseResponse {
    // TODO: do we really need sequence num and primary term? probs delete em
    var id: String
    var seqNo: Long
    var primaryTerm: Long
    var note: Note

    constructor(
        id: String,
        seqNo: Long,
        primaryTerm: Long,
        note: Note
    ) : super() {
        this.id = id
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.note = note
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readString(), // id
        sin.readLong(), // seqNo
        sin.readLong(), // primaryTerm
        Note.readFrom(sin) // note
    )

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        note.writeTo(out)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
            .field(_ID, id)
            .field(_SEQ_NO, seqNo)
            .field(_PRIMARY_TERM, primaryTerm)
            .field("note", note)
            .endObject()
    }
}
