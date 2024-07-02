package org.opensearch.commons.alerting.model

import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.UUIDs
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.commons.alerting.model.action.ActionExecutionPolicy
import org.opensearch.commons.alerting.model.action.Throttle
import org.opensearch.commons.alerting.model.remote.monitors.RemoteDocLevelMonitorInput
import org.opensearch.commons.alerting.model.remote.monitors.RemoteMonitorInput
import org.opensearch.commons.alerting.model.remote.monitors.RemoteMonitorTrigger
import org.opensearch.commons.alerting.randomAction
import org.opensearch.commons.alerting.randomActionExecutionPolicy
import org.opensearch.commons.alerting.randomBucketLevelMonitorRunResult
import org.opensearch.commons.alerting.randomBucketLevelTrigger
import org.opensearch.commons.alerting.randomBucketLevelTriggerRunResult
import org.opensearch.commons.alerting.randomChainedAlertTrigger
import org.opensearch.commons.alerting.randomDocLevelQuery
import org.opensearch.commons.alerting.randomDocumentLevelMonitorRunResult
import org.opensearch.commons.alerting.randomDocumentLevelTrigger
import org.opensearch.commons.alerting.randomInputRunResults
import org.opensearch.commons.alerting.randomQueryLevelMonitor
import org.opensearch.commons.alerting.randomQueryLevelMonitorRunResult
import org.opensearch.commons.alerting.randomQueryLevelTrigger
import org.opensearch.commons.alerting.randomQueryLevelTriggerRunResult
import org.opensearch.commons.alerting.randomThrottle
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.randomUserEmpty
import org.opensearch.commons.alerting.util.IndexUtils
import org.opensearch.commons.authuser.User
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.test.OpenSearchTestCase
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertTrue

class WriteableTests {

    @Test
    fun `test throttle as stream`() {
        val throttle = randomThrottle()
        val out = BytesStreamOutput()
        throttle.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newThrottle = Throttle(sin)
        Assertions.assertEquals(throttle, newThrottle, "Round tripping Throttle doesn't work")
    }

    @Test
    fun `test action as stream`() {
        val action = randomAction()
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        Assertions.assertEquals(action, newAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test action as stream with null subject template`() {
        val action = randomAction().copy(subjectTemplate = null)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        Assertions.assertEquals(action, newAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test action as stream with null throttle`() {
        val action = randomAction().copy(throttle = null)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        Assertions.assertEquals(action, newAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test action as stream with throttled enabled and null throttle`() {
        val action = randomAction().copy(throttle = null).copy(throttleEnabled = true)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        Assertions.assertEquals(action, newAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test query-level monitor as stream`() {
        val monitor = randomQueryLevelMonitor().copy(inputs = listOf(SearchInput(emptyList(), SearchSourceBuilder())))
        val out = BytesStreamOutput()
        monitor.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newMonitor = Monitor(sin)
        Assertions.assertEquals(monitor, newMonitor, "Round tripping QueryLevelMonitor doesn't work")
    }

    @Test
    fun `test query-level trigger as stream`() {
        val trigger = randomQueryLevelTrigger()
        val out = BytesStreamOutput()
        trigger.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newTrigger = QueryLevelTrigger.readFrom(sin)
        Assertions.assertEquals(trigger, newTrigger, "Round tripping QueryLevelTrigger doesn't work")
    }

    @Test
    fun `test bucket-level trigger as stream`() {
        val trigger = randomBucketLevelTrigger()
        val out = BytesStreamOutput()
        trigger.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newTrigger = BucketLevelTrigger.readFrom(sin)
        Assertions.assertEquals(trigger, newTrigger, "Round tripping BucketLevelTrigger doesn't work")
    }

    @Test
    fun `test doc-level trigger as stream`() {
        val trigger = randomDocumentLevelTrigger()
        val out = BytesStreamOutput()
        trigger.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newTrigger = DocumentLevelTrigger.readFrom(sin)
        Assertions.assertEquals(trigger, newTrigger, "Round tripping DocumentLevelTrigger doesn't work")
    }

    @Test
    fun `test doc-level query as stream`() {
        val dlq = randomDocLevelQuery()
        val out = BytesStreamOutput()
        dlq.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDlq = DocLevelQuery.readFrom(sin)
        Assertions.assertEquals(dlq, newDlq, "Round tripping DocLevelQuery doesn't work")
        assertTrue(newDlq.queryFieldNames.isEmpty())
    }

    @Test
    fun `test doc-level query with query Field Names as stream`() {
        val dlq = randomDocLevelQuery().copy(queryFieldNames = listOf("f1", "f2"))
        val out = BytesStreamOutput()
        dlq.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDlq = DocLevelQuery.readFrom(sin)
        assertTrue(newDlq.queryFieldNames.contains(dlq.queryFieldNames[0]))
        assertTrue(newDlq.queryFieldNames.contains(dlq.queryFieldNames[1]))
        Assertions.assertEquals(dlq, newDlq, "Round tripping DocLevelQuery doesn't work")
    }

    @Test
    fun `test chained alert trigger as stream`() {
        val trigger = randomChainedAlertTrigger()
        val out = BytesStreamOutput()
        trigger.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newTrigger = ChainedAlertTrigger.readFrom(sin)
        Assertions.assertEquals(trigger, newTrigger, "Round tripping DocumentLevelTrigger doesn't work")
    }

    @Test
    fun `test searchinput as stream`() {
        val input = SearchInput(emptyList(), SearchSourceBuilder())
        val out = BytesStreamOutput()
        input.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newInput = SearchInput(sin)
        Assertions.assertEquals(input, newInput, "Round tripping MonitorRunResult doesn't work")
    }

    @Test
    fun `test user as stream`() {
        val user = randomUser()
        val out = BytesStreamOutput()
        user.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newUser = User(sin)
        Assertions.assertEquals(user, newUser, "Round tripping User doesn't work")
    }

    @Test
    fun `test empty user as stream`() {
        val user = randomUserEmpty()
        val out = BytesStreamOutput()
        user.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newUser = User(sin)
        Assertions.assertEquals(user, newUser, "Round tripping User doesn't work")
    }

    @Test
    fun `test action execution policy as stream`() {
        val actionExecutionPolicy = randomActionExecutionPolicy()
        val out = BytesStreamOutput()
        actionExecutionPolicy.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newActionExecutionPolicy = ActionExecutionPolicy.readFrom(sin)
        Assertions.assertEquals(
            actionExecutionPolicy,
            newActionExecutionPolicy,
            "Round tripping ActionExecutionPolicy doesn't work"
        )
    }

    @Test
    fun `test Comment object`() {
        val user = randomUser()
        val createdTime = Instant.now()
        val comment = Comment(
            "123",
            "456",
            "alert",
            "content",
            createdTime,
            null,
            user
        )
        Assertions.assertNotNull(comment)
        val out = BytesStreamOutput()
        comment.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newComment = Comment(sin)
        Assertions.assertEquals("123", newComment.id)
        Assertions.assertEquals("456", newComment.entityId)
        Assertions.assertEquals("alert", newComment.entityType)
        Assertions.assertEquals("content", newComment.content)
        Assertions.assertEquals(createdTime, newComment.createdTime)
        Assertions.assertEquals(user, newComment.user)
    }

    @Test
    fun `test actionrunresult as stream`() {
        val actionRunResult = randomActionRunResult()
        val out = BytesStreamOutput()
        actionRunResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newActionRunResult = ActionRunResult(sin)
        OpenSearchTestCase.assertEquals(
            "Round tripping ActionRunResult doesn't work",
            actionRunResult,
            newActionRunResult
        )
    }

    @Test
    fun `test query-level triggerrunresult as stream`() {
        val runResult = randomQueryLevelTriggerRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = QueryLevelTriggerRunResult(sin)
        OpenSearchTestCase.assertEquals(runResult.triggerName, newRunResult.triggerName)
        OpenSearchTestCase.assertEquals(runResult.triggered, newRunResult.triggered)
        OpenSearchTestCase.assertEquals(runResult.error, newRunResult.error)
        OpenSearchTestCase.assertEquals(runResult.actionResults, newRunResult.actionResults)
    }

    @Test
    fun `test bucket-level triggerrunresult as stream`() {
        val runResult = randomBucketLevelTriggerRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = BucketLevelTriggerRunResult(sin)
        OpenSearchTestCase.assertEquals("Round tripping ActionRunResult doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test doc-level triggerrunresult as stream`() {
        val runResult = randomDocumentLevelTriggerRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = DocumentLevelTriggerRunResult(sin)
        OpenSearchTestCase.assertEquals("Round tripping ActionRunResult doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test inputrunresult as stream`() {
        val runResult = randomInputRunResults()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = InputRunResults.readFrom(sin)
        OpenSearchTestCase.assertEquals("Round tripping InputRunResults doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test query-level monitorrunresult as stream`() {
        val runResult = randomQueryLevelMonitorRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = MonitorRunResult<QueryLevelTriggerRunResult>(sin)
        OpenSearchTestCase.assertEquals("Round tripping MonitorRunResult doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test bucket-level monitorrunresult as stream`() {
        val runResult = randomBucketLevelMonitorRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = MonitorRunResult<BucketLevelTriggerRunResult>(sin)
        OpenSearchTestCase.assertEquals("Round tripping MonitorRunResult doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test doc-level monitorrunresult as stream`() {
        val runResult = randomDocumentLevelMonitorRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = MonitorRunResult<DocumentLevelTriggerRunResult>(sin)
        OpenSearchTestCase.assertEquals("Round tripping MonitorRunResult doesn't work", runResult, newRunResult)
    }

    @Test
    fun `test DocumentLevelTriggerRunResult as stream`() {
        val workflow = randomDocumentLevelTriggerRunResult()
        val out = BytesStreamOutput()
        workflow.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newWorkflow = DocumentLevelTriggerRunResult(sin)
        Assert.assertEquals("Round tripping dltrr failed", newWorkflow, workflow)
    }

    @Test
    fun `test RemoteMonitorInput as stream`() {
        val myMonitorInput = MyMonitorInput(1, "hello", MyMonitorInput(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorInput.writeTo(myObjOut)
        val remoteMonitorInput = RemoteMonitorInput(myObjOut.bytes())

        val out = BytesStreamOutput()
        remoteMonitorInput.writeTo(out)

        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRemoteMonitorInput = RemoteMonitorInput(sin)
        val newMyMonitorInput = MyMonitorInput(StreamInput.wrap(newRemoteMonitorInput.input.toBytesRef().bytes))
        Assert.assertEquals("Round tripping RemoteMonitorInput failed", newMyMonitorInput, myMonitorInput)
    }

    @Test
    fun `test RemoteMonitorTrigger as stream`() {
        val myMonitorTrigger = MyMonitorTrigger(1, "hello", MyMonitorTrigger(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorTrigger.writeTo(myObjOut)
        val remoteMonitorTrigger = RemoteMonitorTrigger("id", "name", "1", listOf(), myObjOut.bytes())

        val out = BytesStreamOutput()
        remoteMonitorTrigger.writeTo(out)

        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRemoteMonitorTrigger = RemoteMonitorTrigger(sin)
        val newMyMonitorTrigger = MyMonitorTrigger(StreamInput.wrap(newRemoteMonitorTrigger.trigger.toBytesRef().bytes))
        Assert.assertEquals("Round tripping RemoteMonitorTrigger failed", newMyMonitorTrigger, myMonitorTrigger)
    }

    @Test
    fun `test RemoteDocLevelMonitorInput as stream`() {
        val myMonitorInput = MyMonitorInput(1, "hello", MyMonitorInput(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorInput.writeTo(myObjOut)
        val docLevelMonitorInput = DocLevelMonitorInput(
            "test",
            listOf("test"),
            listOf(randomDocLevelQuery())
        )
        val remoteDocLevelMonitorInput = RemoteDocLevelMonitorInput(myObjOut.bytes(), docLevelMonitorInput)

        val out = BytesStreamOutput()
        remoteDocLevelMonitorInput.writeTo(out)

        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRemoteDocLevelMonitorInput = RemoteDocLevelMonitorInput(sin)
        val newMyMonitorInput = MyMonitorInput(StreamInput.wrap(newRemoteDocLevelMonitorInput.input.toBytesRef().bytes))
        Assert.assertEquals("Round tripping RemoteMonitorInput failed", newMyMonitorInput, myMonitorInput)
        val newDocLevelMonitorInput = newRemoteDocLevelMonitorInput.docLevelMonitorInput
        Assert.assertEquals("Round tripping DocLevelMonitorInput failed", newDocLevelMonitorInput, docLevelMonitorInput)
    }

    @Test
    fun `test RemoteMonitor as stream`() {
        val myMonitorInput = MyMonitorInput(1, "hello", MyMonitorInput(2, "world", null))
        var myObjOut = BytesStreamOutput()
        myMonitorInput.writeTo(myObjOut)
        val docLevelMonitorInput = DocLevelMonitorInput(
            "test",
            listOf("test"),
            listOf(randomDocLevelQuery())
        )
        val remoteDocLevelMonitorInput = RemoteDocLevelMonitorInput(myObjOut.bytes(), docLevelMonitorInput)

        val myMonitorTrigger = MyMonitorTrigger(1, "hello", MyMonitorTrigger(2, "world", null))
        myObjOut = BytesStreamOutput()
        myMonitorTrigger.writeTo(myObjOut)
        val remoteMonitorTrigger = RemoteMonitorTrigger("id", "name", "1", listOf(), myObjOut.bytes())

        val monitor = Monitor(
            Monitor.NO_ID,
            Monitor.NO_VERSION,
            "hello",
            true,
            IntervalSchedule(1, ChronoUnit.MINUTES),
            Instant.now(),
            Instant.now(),
            "remote_doc_level_monitor",
            null,
            IndexUtils.NO_SCHEMA_VERSION,
            listOf(remoteDocLevelMonitorInput),
            listOf(remoteMonitorTrigger),
            mapOf()
        )

        val out = BytesStreamOutput()
        monitor.writeTo(out)

        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newMonitor = Monitor(sin)
        Assert.assertEquals("Round tripping RemoteMonitor failed", monitor, newMonitor)
    }

    fun randomDocumentLevelTriggerRunResult(): DocumentLevelTriggerRunResult {
        val map = mutableMapOf<String, ActionRunResult>()
        map.plus(Pair("key1", randomActionRunResult()))
        map.plus(Pair("key2", randomActionRunResult()))
        return DocumentLevelTriggerRunResult(
            "trigger-name",
            mutableListOf(UUIDs.randomBase64UUID().toString()),
            null,
            mutableMapOf(Pair("alertId", map))
        )
    }

    fun randomActionRunResult(): ActionRunResult {
        val map = mutableMapOf<String, String>()
        map.plus(Pair("key1", "val1"))
        map.plus(Pair("key2", "val2"))
        return ActionRunResult(
            "1234",
            "test-action",
            map,
            false,
            Instant.now(),
            null
        )
    }
}

data class MyMonitorInput(val a: Int, val b: String, val c: MyMonitorInput?) : Writeable {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readInt(),
        sin.readString(),
        sin.readOptionalWriteable { MyMonitorInput(it) }
    )

    override fun writeTo(out: StreamOutput) {
        out.writeInt(a)
        out.writeString(b)
        out.writeOptionalWriteable(c)
    }
}

data class MyMonitorTrigger(val a: Int, val b: String, val c: MyMonitorTrigger?) : Writeable {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        sin.readInt(),
        sin.readString(),
        sin.readOptionalWriteable { MyMonitorTrigger(it) }
    )

    override fun writeTo(out: StreamOutput) {
        out.writeInt(a)
        out.writeString(b)
        out.writeOptionalWriteable(c)
    }
}
