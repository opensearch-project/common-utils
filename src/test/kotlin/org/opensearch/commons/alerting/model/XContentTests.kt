package org.opensearch.commons.alerting.model

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.json.JsonXContent
import org.opensearch.commons.alerting.builder
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.commons.alerting.model.action.ActionExecutionPolicy
import org.opensearch.commons.alerting.model.action.PerExecutionActionScope
import org.opensearch.commons.alerting.model.action.Throttle
import org.opensearch.commons.alerting.model.remote.monitors.RemoteDocLevelMonitorInput
import org.opensearch.commons.alerting.model.remote.monitors.RemoteMonitorInput
import org.opensearch.commons.alerting.model.remote.monitors.RemoteMonitorTrigger
import org.opensearch.commons.alerting.parser
import org.opensearch.commons.alerting.randomAction
import org.opensearch.commons.alerting.randomActionExecutionPolicy
import org.opensearch.commons.alerting.randomActionExecutionResult
import org.opensearch.commons.alerting.randomActionWithPolicy
import org.opensearch.commons.alerting.randomAlert
import org.opensearch.commons.alerting.randomBucketLevelMonitor
import org.opensearch.commons.alerting.randomBucketLevelTrigger
import org.opensearch.commons.alerting.randomDocLevelQuery
import org.opensearch.commons.alerting.randomQueryLevelMonitor
import org.opensearch.commons.alerting.randomQueryLevelMonitorWithoutUser
import org.opensearch.commons.alerting.randomQueryLevelTrigger
import org.opensearch.commons.alerting.randomThrottle
import org.opensearch.commons.alerting.randomUser
import org.opensearch.commons.alerting.randomUserEmpty
import org.opensearch.commons.alerting.randomWorkflow
import org.opensearch.commons.alerting.toJsonString
import org.opensearch.commons.alerting.toJsonStringWithUser
import org.opensearch.commons.alerting.util.string
import org.opensearch.commons.authuser.User
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.index.query.QueryBuilders
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.test.OpenSearchTestCase
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertFailsWith

class XContentTests {

    @Test
    fun `test action parsing`() {
        val action = randomAction()
        val actionString = action.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedAction = Action.parse(parser(actionString))
        Assertions.assertEquals(action, parsedAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test action parsing with null subject template`() {
        val action = randomAction().copy(subjectTemplate = null)
        val actionString = action.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedAction = Action.parse(parser(actionString))
        Assertions.assertEquals(action, parsedAction, "Round tripping Action doesn't work")
    }

    @Test
    fun `test action parsing with null throttle`() {
        val action = randomAction().copy(throttle = null)
        val actionString = action.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedAction = Action.parse(parser(actionString))
        Assertions.assertEquals(action, parsedAction, "Round tripping Action doesn't work")
    }

    fun `test action parsing with throttled enabled and null throttle`() {
        val action = randomAction().copy(throttle = null).copy(throttleEnabled = true)
        val actionString = action.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        assertFailsWith<IllegalArgumentException>("Action throttle enabled but not set throttle value") {
            Action.parse(parser(actionString))
        }
    }

    @Test
    fun `test action with per execution scope does not support throttling`() {
        try {
            randomActionWithPolicy().copy(
                throttleEnabled = true,
                throttle = Throttle(value = 5, unit = ChronoUnit.MINUTES),
                actionExecutionPolicy = ActionExecutionPolicy(PerExecutionActionScope())
            )
            Assertions.fail("Creating an action with per execution scope and throttle enabled did not fail.")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun `test throttle parsing`() {
        val throttle = randomThrottle()
        val throttleString = throttle.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedThrottle = Throttle.parse(parser(throttleString))
        Assertions.assertEquals(throttle, parsedThrottle, "Round tripping Monitor doesn't work")
    }

    @Test
    fun `test throttle parsing with wrong unit`() {
        val throttle = randomThrottle()
        val throttleString = throttle.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val wrongThrottleString = throttleString.replace("MINUTES", "wrongunit")

        assertFailsWith<IllegalArgumentException>("Only support MINUTES throttle unit") {
            Throttle.parse(
                parser(
                    wrongThrottleString
                )
            )
        }
    }

    @Test
    fun `test throttle parsing with negative value`() {
        val throttle = randomThrottle().copy(value = -1)
        val throttleString = throttle.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()

        assertFailsWith<IllegalArgumentException>("Can only set positive throttle period") {
            Throttle.parse(
                parser(
                    throttleString
                )
            )
        }
    }

    fun `test query-level monitor parsing`() {
        val monitor = randomQueryLevelMonitor()

        val monitorString = monitor.toJsonStringWithUser()
        val parsedMonitor = Monitor.parse(parser(monitorString))
        assertEquals("Round tripping QueryLevelMonitor doesn't work", monitor, parsedMonitor)
    }

    @Test
    fun `test monitor parsing with no name`() {
        val monitorStringWithoutName = """
            {
              "type": "monitor",
              "enabled": false,
              "schedule": {
                "period": {
                  "interval": 1,
                  "unit": "MINUTES"
                }
              },
              "inputs": [],
              "triggers": []
            }
        """.trimIndent()

        assertFailsWith<IllegalArgumentException>("Monitor name is null") {
            Monitor.parse(
                parser(
                    monitorStringWithoutName
                )
            )
        }
    }

    @Test
    fun `test monitor parsing with no schedule`() {
        val monitorStringWithoutSchedule = """
            {
              "type": "monitor",
              "name": "asdf",
              "enabled": false,
              "inputs": [],
              "triggers": []
            }
        """.trimIndent()

        assertFailsWith<IllegalArgumentException>("Monitor schedule is null") {
            Monitor.parse(parser(monitorStringWithoutSchedule))
        }
    }

    @Test
    fun `test bucket-level monitor parsing`() {
        val monitor = randomBucketLevelMonitor()

        val monitorString = monitor.toJsonStringWithUser()
        val parsedMonitor = Monitor.parse(parser(monitorString))
        Assertions.assertEquals(monitor, parsedMonitor, "Round tripping BucketLevelMonitor doesn't work")
    }

    @Test
    fun `test composite workflow parsing`() {
        val workflow = randomWorkflow()
        val monitorString = workflow.toJsonStringWithUser()
        val parsedMonitor = Workflow.parse(parser(monitorString))
        Assertions.assertEquals(workflow, parsedMonitor, "Round tripping BucketLevelMonitor doesn't work")
    }

    @Test
    fun `test composite workflow parsing with auditDelegateMonitorAlerts flag disabled`() {
        val workflow = randomWorkflow(auditDelegateMonitorAlerts = false)
        val monitorString = workflow.toJsonStringWithUser()
        val parsedMonitor = Workflow.parse(parser(monitorString))
        Assertions.assertEquals(workflow, parsedMonitor, "Round tripping BucketLevelMonitor doesn't work")
    }

    @Test
    fun `test query-level trigger parsing`() {
        val trigger = randomQueryLevelTrigger()

        val triggerString = trigger.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedTrigger = Trigger.parse(parser(triggerString))

        Assertions.assertEquals(trigger, parsedTrigger, "Round tripping QueryLevelTrigger doesn't work")
    }

    @Test
    fun `test bucket-level trigger parsing`() {
        val trigger = randomBucketLevelTrigger()

        val triggerString = trigger.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedTrigger = Trigger.parse(parser(triggerString))

        Assertions.assertEquals(trigger, parsedTrigger, "Round tripping BucketLevelTrigger doesn't work")
    }

    @Test
    fun `test no-op trigger parsing`() {
        val trigger = NoOpTrigger()

        val triggerString = trigger.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedTrigger = Trigger.parse(parser(triggerString))

        Assertions.assertEquals(trigger, parsedTrigger, "Round tripping NoOpTrigger doesn't work")
    }

    @Test
    fun `test creating a monitor with duplicate trigger ids fails`() {
        try {
            val repeatedTrigger = randomQueryLevelTrigger()
            randomQueryLevelMonitor().copy(triggers = listOf(repeatedTrigger, repeatedTrigger))
            Assertions.fail("Creating a monitor with duplicate triggers did not fail.")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun `test user parsing`() {
        val user = randomUser()
        val userString = user.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedUser = User.parse(parser(userString))
        Assertions.assertEquals(user, parsedUser, "Round tripping user doesn't work")
    }

    @Test
    fun `test empty user parsing`() {
        val user = randomUserEmpty()
        val userString = user.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()

        val parsedUser = User.parse(parser(userString))
        Assertions.assertEquals(user, parsedUser, "Round tripping user doesn't work")
        Assertions.assertEquals("", parsedUser.name)
        Assertions.assertEquals(0, parsedUser.roles.size)
    }

    @Test
    fun `test query-level monitor parsing without user`() {
        val monitor = randomQueryLevelMonitorWithoutUser()

        val monitorString = monitor.toJsonString()
        val parsedMonitor = Monitor.parse(parser(monitorString))
        Assertions.assertEquals(monitor, parsedMonitor, "Round tripping QueryLevelMonitor doesn't work")
        Assertions.assertNull(parsedMonitor.user)
    }

    @Test
    fun `test workflow parsing`() {
        val workflow = randomWorkflow(monitorIds = listOf("1", "2", "3"))
        val monitorString = workflow.toJsonString()
        val parsedWorkflow = Workflow.parse(parser(monitorString))
        Assertions.assertEquals(workflow, parsedWorkflow, "Round tripping workflow failed")
    }

    @Test
    fun `test chainedMonitorFindings parsing`() {
        val cmf1 = ChainedMonitorFindings(monitorId = "m1")
        val cmf1String = cmf1.toJsonString()
        Assertions.assertEquals(
            ChainedMonitorFindings.parse(parser(cmf1String)),
            cmf1,
            "Round tripping chained monitor findings failed"
        )
        val cmf2 = ChainedMonitorFindings(monitorIds = listOf("m1", "m2"))
        val cmf2String = cmf2.toJsonString()
        Assertions.assertEquals(
            ChainedMonitorFindings.parse(parser(cmf2String)),
            cmf2,
            "Round tripping chained monitor findings failed"
        )
    }

    @Test
    fun `test old monitor format parsing`() {
        val monitorString = """
            {
              "type": "monitor",
              "schema_version": 3,
              "name": "asdf",
              "user": {
                "name": "admin123",
                "backend_roles": [],
                "roles": [
                  "all_access",
                  "security_manager"
                ],
                "custom_attribute_names": [],
                "user_requested_tenant": null
              },
              "enabled": true,
              "enabled_time": 1613530078244,
              "schedule": {
                "period": {
                  "interval": 1,
                  "unit": "MINUTES"
                }
              },
              "inputs": [
                {
                  "search": {
                    "indices": [
                      "test_index"
                    ],
                    "query": {
                      "size": 0,
                      "query": {
                        "bool": {
                          "filter": [
                            {
                              "range": {
                                "order_date": {
                                  "from": "{{period_end}}||-1h",
                                  "to": "{{period_end}}",
                                  "include_lower": true,
                                  "include_upper": true,
                                  "format": "epoch_millis",
                                  "boost": 1.0
                                }
                              }
                            }
                          ],
                          "adjust_pure_negative": true,
                          "boost": 1.0
                        }
                      },
                      "aggregations": {}
                    }
                  }
                }
              ],
              "triggers": [
                {
                  "id": "e_sc0XcB98Q42rHjTh4K",
                  "name": "abc",
                  "severity": "1",
                  "condition": {
                    "script": {
                      "source": "ctx.results[0].hits.total.value > 100000",
                      "lang": "painless"
                    }
                  },
                  "actions": []
                }
              ],
              "last_update_time": 1614121489719
            }
        """.trimIndent()
        val parsedMonitor = Monitor.parse(parser(monitorString))
        Assertions.assertEquals(
            Monitor.MonitorType.QUERY_LEVEL_MONITOR.value,
            parsedMonitor.monitorType,
            "Incorrect monitor type"
        )
        Assertions.assertEquals(1, parsedMonitor.triggers.size, "Incorrect trigger count")
        val trigger = parsedMonitor.triggers.first()
        Assertions.assertTrue(trigger is QueryLevelTrigger, "Incorrect trigger type")
        Assertions.assertEquals("abc", trigger.name, "Incorrect name for parsed trigger")
    }

    @Test
    fun `test creating an query-level monitor with invalid trigger type fails`() {
        try {
            val bucketLevelTrigger = randomBucketLevelTrigger()
            randomQueryLevelMonitor().copy(triggers = listOf(bucketLevelTrigger))
            Assertions.fail("Creating a query-level monitor with bucket-level triggers did not fail.")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun `test creating an bucket-level monitor with invalid trigger type fails`() {
        try {
            val queryLevelTrigger = randomQueryLevelTrigger()
            randomBucketLevelMonitor().copy(triggers = listOf(queryLevelTrigger))
            Assertions.fail("Creating a bucket-level monitor with query-level triggers did not fail.")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun `test creating an bucket-level monitor with invalid input fails`() {
        try {
            val invalidInput = SearchInput(emptyList(), SearchSourceBuilder().query(QueryBuilders.matchAllQuery()))
            randomBucketLevelMonitor().copy(inputs = listOf(invalidInput))
            Assertions.fail("Creating an bucket-level monitor with an invalid input did not fail.")
        } catch (ignored: IllegalArgumentException) {
        }
    }

    @Test
    fun `test action execution policy`() {
        val actionExecutionPolicy = randomActionExecutionPolicy()
        val actionExecutionPolicyString = actionExecutionPolicy.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedActionExecutionPolicy = ActionExecutionPolicy.parse(parser(actionExecutionPolicyString))
        Assertions.assertEquals(
            actionExecutionPolicy,
            parsedActionExecutionPolicy,
            "Round tripping ActionExecutionPolicy doesn't work"
        )
    }

    @Test
    fun `test doc level query toXcontent`() {
        val dlq = DocLevelQuery("id", "name", listOf("f1", "f2"), "query", listOf("t1", "t2"))
        val dlqString = dlq.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedDlq = DocLevelQuery.parse(parser(dlqString))
        Assertions.assertEquals(
            dlq,
            parsedDlq,
            "Round tripping Doc level query doesn't work"
        )
    }

    @Test
    fun `test doc level query toXcontent with query field names`() {
        val dlq = DocLevelQuery("id", "name", listOf("f1", "f2"), "query", listOf("t1", "t2"), listOf("f1", "f2"))
        val dlqString = dlq.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedDlq = DocLevelQuery.parse(parser(dlqString))
        Assertions.assertEquals(
            dlq,
            parsedDlq,
            "Round tripping Doc level query doesn't work"
        )
    }

    @Test
    fun `test alert parsing`() {
        val alert = randomAlert()

        val alertString = alert.toXContentWithUser(builder()).string()
        val parsedAlert = Alert.parse(parser(alertString))

        assertEquals("Round tripping alert doesn't work", alert, parsedAlert)
    }

    @Test
    fun `test alert parsing with noop trigger`() {
        val monitor = randomQueryLevelMonitor()
        val alert = Alert(
            id = "",
            monitor = monitor,
            trigger = NoOpTrigger(),
            startTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
            errorMessage = "some error",
            lastNotificationTime = Instant.now(),
            workflowId = "",
            executionId = "",
            clusters = listOf()
        )
        assertEquals("Round tripping alert doesn't work", alert.triggerName, "NoOp trigger")
    }

    @Test
    fun `test alert parsing without user`() {
        val alertStr = "{\"id\":\"\",\"version\":-1,\"monitor_id\":\"\",\"schema_version\":0,\"monitor_version\":1," +
            "\"monitor_name\":\"ARahqfRaJG\",\"trigger_id\":\"fhe1-XQBySl0wQKDBkOG\",\"trigger_name\":\"ffELMuhlro\"," +
            "\"state\":\"ACTIVE\",\"error_message\":null,\"alert_history\":[],\"severity\":\"1\",\"action_execution_results\"" +
            ":[{\"action_id\":\"ghe1-XQBySl0wQKDBkOG\",\"last_execution_time\":1601917224583,\"throttled_count\":-1478015168}," +
            "{\"action_id\":\"gxe1-XQBySl0wQKDBkOH\",\"last_execution_time\":1601917224583,\"throttled_count\":-768533744}]," +
            "\"start_time\":1601917224599,\"last_notification_time\":null,\"end_time\":null,\"acknowledged_time\":null," +
            "\"clusters\":[\"cluster-1\",\"cluster-2\"]}"
        val parsedAlert = Alert.parse(parser(alertStr))
        OpenSearchTestCase.assertNull(parsedAlert.monitorUser)
    }

    @Test
    fun `test alert parsing with user as null`() {
        val alertStr =
            "{\"id\":\"\",\"version\":-1,\"monitor_id\":\"\",\"schema_version\":0,\"monitor_version\":1,\"monitor_user\":null," +
                "\"monitor_name\":\"ARahqfRaJG\",\"trigger_id\":\"fhe1-XQBySl0wQKDBkOG\",\"trigger_name\":\"ffELMuhlro\"," +
                "\"state\":\"ACTIVE\",\"error_message\":null,\"alert_history\":[],\"severity\":\"1\",\"action_execution_results\"" +
                ":[{\"action_id\":\"ghe1-XQBySl0wQKDBkOG\",\"last_execution_time\":1601917224583,\"throttled_count\":-1478015168}," +
                "{\"action_id\":\"gxe1-XQBySl0wQKDBkOH\",\"last_execution_time\":1601917224583,\"throttled_count\":-768533744}]," +
                "\"start_time\":1601917224599,\"last_notification_time\":null,\"end_time\":null,\"acknowledged_time\":null," +
                "\"clusters\":[\"cluster-1\",\"cluster-2\"]}"
        val parsedAlert = Alert.parse(parser(alertStr))
        OpenSearchTestCase.assertNull(parsedAlert.monitorUser)
    }

    @Test
    fun `test action execution result parsing`() {
        val actionExecutionResult = randomActionExecutionResult()

        val actionExecutionResultString = actionExecutionResult.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedActionExecutionResultString = ActionExecutionResult.parse(parser(actionExecutionResultString))

        assertEquals("Round tripping alert doesn't work", actionExecutionResult, parsedActionExecutionResultString)
    }

    @Test
    fun `test MonitorMetadata`() {
        val monitorMetadata = MonitorMetadata(
            id = "monitorId-metadata",
            monitorId = "monitorId",
            lastActionExecutionTimes = emptyList(),
            lastRunContext = emptyMap(),
            sourceToQueryIndexMapping = mutableMapOf()
        )
        val monitorMetadataString = monitorMetadata.toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS).string()
        val parsedMonitorMetadata = MonitorMetadata.parse(parser(monitorMetadataString))
        assertEquals("Round tripping MonitorMetadata doesn't work", monitorMetadata, parsedMonitorMetadata)
    }

    @Test
    fun `test RemoteMonitorInput`() {
        val myMonitorInput = MyMonitorInput(1, "hello", MyMonitorInput(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorInput.writeTo(myObjOut)
        val remoteMonitorInput = RemoteMonitorInput(myObjOut.bytes())

        val xContent = remoteMonitorInput.toXContent(JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS).string()
        val parsedRemoteMonitorInput = RemoteMonitorInput.parse(parser(xContent))
        val parsedMyMonitorInput = MyMonitorInput(StreamInput.wrap(parsedRemoteMonitorInput.input.toBytesRef().bytes))
        assertEquals("Round tripping RemoteMonitorInput doesn't work", myMonitorInput, parsedMyMonitorInput)
    }

    @Test
    fun `test RemoteMonitorTrigger`() {
        val myMonitorTrigger = MyMonitorTrigger(1, "hello", MyMonitorTrigger(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorTrigger.writeTo(myObjOut)
        val remoteMonitorTrigger = RemoteMonitorTrigger("id", "name", "1", listOf(), myObjOut.bytes())

        val xContent = remoteMonitorTrigger.toXContent(JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS).string()
        val parsedRemoteMonitorTrigger = Trigger.parse(parser(xContent)) as RemoteMonitorTrigger
        val parsedMyMonitorTrigger = MyMonitorTrigger(StreamInput.wrap(parsedRemoteMonitorTrigger.trigger.toBytesRef().bytes))
        assertEquals("Round tripping RemoteMonitorTrigger doesn't work", myMonitorTrigger, parsedMyMonitorTrigger)
    }

    @Test
    fun `test RemoteDocLevelMonitorInput`() {
        val myMonitorInput = MyMonitorInput(1, "hello", MyMonitorInput(2, "world", null))
        val myObjOut = BytesStreamOutput()
        myMonitorInput.writeTo(myObjOut)
        val docLevelMonitorInput = DocLevelMonitorInput(
            "test",
            listOf("test"),
            listOf(randomDocLevelQuery())
        )
        val remoteDocLevelMonitorInput = RemoteDocLevelMonitorInput(myObjOut.bytes(), docLevelMonitorInput)

        val xContent = remoteDocLevelMonitorInput.toXContent(JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS).string()
        val parsedRemoteDocLevelMonitorInput = RemoteDocLevelMonitorInput.parse(parser(xContent))
        val parsedMyMonitorInput = MyMonitorInput(StreamInput.wrap(parsedRemoteDocLevelMonitorInput.input.toBytesRef().bytes))
        assertEquals("Round tripping RemoteDocLevelMonitorInput doesn't work", myMonitorInput, parsedMyMonitorInput)
        val parsedDocLevelMonitorInput = parsedRemoteDocLevelMonitorInput.docLevelMonitorInput
        assertEquals("Round tripping RemoteDocLevelMonitorInput doesn't work", docLevelMonitorInput, parsedDocLevelMonitorInput)
    }

    @Test
    fun `test DataSources parsing`() {
        val dataSources = DataSources(
            ScheduledJob.DOC_LEVEL_QUERIES_INDEX,
            ".opensearch-alerting-finding-history-write",
            "<.opensearch-alerting-finding-history-{now/d}-1>",
            ".opendistro-alerting-alerts",
            ".opendistro-alerting-alert-history-write",
            "<.opendistro-alerting-alert-history-{now/d}-1>",
            mapOf(),
            false
        )
        Assertions.assertNotNull(dataSources)

        val dataSourcesString = dataSources.toXContent(builder(), ToXContent.EMPTY_PARAMS).string()
        val parsedDataSources = DataSources.parse(parser(dataSourcesString))
        Assertions.assertEquals(dataSources, parsedDataSources, "Round tripping DataSources doesn't work")
    }

    @Test
    fun `test Comment parsing`() {
        val comment = Comment(
            "123",
            "456",
            "alert",
            "content",
            Instant.now().truncatedTo(ChronoUnit.MILLIS),
            null,
            randomUser()
        )
        Assertions.assertNotNull(comment)

        val commentString = comment.toXContentWithUser(builder()).string()
        val parsedComment = Comment.parse(parser(commentString), "123")
        Assertions.assertEquals(comment, parsedComment, "Round tripping Comment doesn't work")
    }
}
