package org.opensearch.commons.alerting

import com.carrotsearch.randomizedtesting.generators.RandomNumbers
import com.carrotsearch.randomizedtesting.generators.RandomStrings
import junit.framework.TestCase.assertNull
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.client.Response
import org.opensearch.client.RestClient
import org.opensearch.client.WarningsHandler
import org.opensearch.common.UUIDs
import org.opensearch.common.settings.Settings
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentType
import org.opensearch.commons.alerting.aggregation.bucketselectorext.BucketSelectorExtAggregationBuilder
import org.opensearch.commons.alerting.aggregation.bucketselectorext.BucketSelectorExtFilter
import org.opensearch.commons.alerting.model.BucketLevelTrigger
import org.opensearch.commons.alerting.model.ClusterMetricsInput
import org.opensearch.commons.alerting.model.DocLevelMonitorInput
import org.opensearch.commons.alerting.model.DocLevelQuery
import org.opensearch.commons.alerting.model.DocumentLevelTrigger
import org.opensearch.commons.alerting.model.Input
import org.opensearch.commons.alerting.model.IntervalSchedule
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.model.QueryLevelTrigger
import org.opensearch.commons.alerting.model.Schedule
import org.opensearch.commons.alerting.model.SearchInput
import org.opensearch.commons.alerting.model.Trigger
import org.opensearch.commons.alerting.model.action.Action
import org.opensearch.commons.alerting.model.action.ActionExecutionPolicy
import org.opensearch.commons.alerting.model.action.ActionExecutionScope
import org.opensearch.commons.alerting.model.action.AlertCategory
import org.opensearch.commons.alerting.model.action.PerAlertActionScope
import org.opensearch.commons.alerting.model.action.PerExecutionActionScope
import org.opensearch.commons.alerting.model.action.Throttle
import org.opensearch.commons.alerting.util.string
import org.opensearch.commons.authuser.User
import org.opensearch.index.query.QueryBuilders
import org.opensearch.script.Script
import org.opensearch.script.ScriptType
import org.opensearch.search.SearchModule
import org.opensearch.search.aggregations.bucket.terms.IncludeExclude
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.opensearch.search.builder.SearchSourceBuilder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Random

const val ALL_ACCESS_ROLE = "all_access"

fun randomQueryLevelMonitor(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    user: User = randomUser(),
    inputs: List<Input> = listOf(SearchInput(emptyList(), SearchSourceBuilder().query(QueryBuilders.matchAllQuery()))),
    schedule: Schedule = IntervalSchedule(interval = 5, unit = ChronoUnit.MINUTES),
    enabled: Boolean = Random().nextBoolean(),
    triggers: List<Trigger> = (1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomQueryLevelTrigger() },
    enabledTime: Instant? = if (enabled) Instant.now().truncatedTo(ChronoUnit.MILLIS) else null,
    lastUpdateTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    withMetadata: Boolean = false
): Monitor {
    return Monitor(
        name = name, monitorType = Monitor.MonitorType.QUERY_LEVEL_MONITOR, enabled = enabled, inputs = inputs,
        schedule = schedule, triggers = triggers, enabledTime = enabledTime, lastUpdateTime = lastUpdateTime, user = user,
        uiMetadata = if (withMetadata) mapOf("foo" to "bar") else mapOf()
    )
}

// Monitor of older versions without security.
fun randomQueryLevelMonitorWithoutUser(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    inputs: List<Input> = listOf(SearchInput(emptyList(), SearchSourceBuilder().query(QueryBuilders.matchAllQuery()))),
    schedule: Schedule = IntervalSchedule(interval = 5, unit = ChronoUnit.MINUTES),
    enabled: Boolean = Random().nextBoolean(),
    triggers: List<Trigger> = (1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomQueryLevelTrigger() },
    enabledTime: Instant? = if (enabled) Instant.now().truncatedTo(ChronoUnit.MILLIS) else null,
    lastUpdateTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    withMetadata: Boolean = false
): Monitor {
    return Monitor(
        name = name, monitorType = Monitor.MonitorType.QUERY_LEVEL_MONITOR, enabled = enabled, inputs = inputs,
        schedule = schedule, triggers = triggers, enabledTime = enabledTime, lastUpdateTime = lastUpdateTime, user = null,
        uiMetadata = if (withMetadata) mapOf("foo" to "bar") else mapOf()
    )
}

fun randomBucketLevelMonitor(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    user: User = randomUser(),
    inputs: List<Input> = listOf(
        SearchInput(
            emptyList(),
            SearchSourceBuilder().query(QueryBuilders.matchAllQuery())
                .aggregation(TermsAggregationBuilder("test_agg").field("test_field"))
        )
    ),
    schedule: Schedule = IntervalSchedule(interval = 5, unit = ChronoUnit.MINUTES),
    enabled: Boolean = Random().nextBoolean(),
    triggers: List<Trigger> = (1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomBucketLevelTrigger() },
    enabledTime: Instant? = if (enabled) Instant.now().truncatedTo(ChronoUnit.MILLIS) else null,
    lastUpdateTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    withMetadata: Boolean = false
): Monitor {
    return Monitor(
        name = name, monitorType = Monitor.MonitorType.BUCKET_LEVEL_MONITOR, enabled = enabled, inputs = inputs,
        schedule = schedule, triggers = triggers, enabledTime = enabledTime, lastUpdateTime = lastUpdateTime, user = user,
        uiMetadata = if (withMetadata) mapOf("foo" to "bar") else mapOf()
    )
}

fun randomClusterMetricsMonitor(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    user: User = randomUser(),
    inputs: List<Input> = listOf(randomClusterMetricsInput()),
    schedule: Schedule = IntervalSchedule(interval = 5, unit = ChronoUnit.MINUTES),
    enabled: Boolean = Random().nextBoolean(),
    triggers: List<Trigger> = (1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomQueryLevelTrigger() },
    enabledTime: Instant? = if (enabled) Instant.now().truncatedTo(ChronoUnit.MILLIS) else null,
    lastUpdateTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    withMetadata: Boolean = false
): Monitor {
    return Monitor(
        name = name, monitorType = Monitor.MonitorType.CLUSTER_METRICS_MONITOR, enabled = enabled, inputs = inputs,
        schedule = schedule, triggers = triggers, enabledTime = enabledTime, lastUpdateTime = lastUpdateTime, user = user,
        uiMetadata = if (withMetadata) mapOf("foo" to "bar") else mapOf()
    )
}

fun randomDocumentLevelMonitor(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    user: User? = randomUser(),
    inputs: List<Input> = listOf(DocLevelMonitorInput("description", listOf("index"), emptyList())),
    schedule: Schedule = IntervalSchedule(interval = 5, unit = ChronoUnit.MINUTES),
    enabled: Boolean = Random().nextBoolean(),
    triggers: List<Trigger> = (1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomQueryLevelTrigger() },
    enabledTime: Instant? = if (enabled) Instant.now().truncatedTo(ChronoUnit.MILLIS) else null,
    lastUpdateTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    withMetadata: Boolean = false
): Monitor {
    return Monitor(
        name = name, monitorType = Monitor.MonitorType.DOC_LEVEL_MONITOR, enabled = enabled, inputs = inputs,
        schedule = schedule, triggers = triggers, enabledTime = enabledTime, lastUpdateTime = lastUpdateTime, user = user,
        uiMetadata = if (withMetadata) mapOf("foo" to "bar") else mapOf()
    )
}

fun randomQueryLevelTrigger(
    id: String = UUIDs.base64UUID(),
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    severity: String = "1",
    condition: Script = randomScript(),
    actions: List<Action> = mutableListOf(),
    destinationId: String = ""
): QueryLevelTrigger {
    return QueryLevelTrigger(
        id = id,
        name = name,
        severity = severity,
        condition = condition,
        actions = if (actions.isEmpty()) (0..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomAction(destinationId = destinationId) } else actions
    )
}

fun randomBucketLevelTrigger(
    id: String = UUIDs.base64UUID(),
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    severity: String = "1",
    bucketSelector: BucketSelectorExtAggregationBuilder = randomBucketSelectorExtAggregationBuilder(name = id),
    actions: List<Action> = mutableListOf(),
    destinationId: String = ""
): BucketLevelTrigger {
    return BucketLevelTrigger(
        id = id,
        name = name,
        severity = severity,
        bucketSelector = bucketSelector,
        actions = if (actions.isEmpty()) randomActionsForBucketLevelTrigger(destinationId = destinationId) else actions
    )
}

fun randomActionsForBucketLevelTrigger(min: Int = 0, max: Int = 10, destinationId: String = ""): List<Action> =
    (min..RandomNumbers.randomIntBetween(Random(), 0, max)).map { randomActionWithPolicy(destinationId = destinationId) }

fun randomDocumentLevelTrigger(
    id: String = UUIDs.base64UUID(),
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    severity: String = "1",
    condition: Script = randomScript(),
    actions: List<Action> = mutableListOf(),
    destinationId: String = ""
): DocumentLevelTrigger {
    return DocumentLevelTrigger(
        id = id,
        name = name,
        severity = severity,
        condition = condition,
        actions = if (actions.isEmpty() && destinationId.isNotBlank())
            (0..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomAction(destinationId = destinationId) }
        else actions
    )
}

fun randomBucketSelectorExtAggregationBuilder(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    bucketsPathsMap: MutableMap<String, String> = mutableMapOf("avg" to "10"),
    script: Script = randomBucketSelectorScript(params = bucketsPathsMap),
    parentBucketPath: String = "testPath",
    filter: BucketSelectorExtFilter = BucketSelectorExtFilter(IncludeExclude("foo*", "bar*"))
): BucketSelectorExtAggregationBuilder {
    return BucketSelectorExtAggregationBuilder(name, bucketsPathsMap, script, parentBucketPath, filter)
}

fun randomBucketSelectorScript(
    idOrCode: String = "params.avg >= 0",
    params: Map<String, String> = mutableMapOf("avg" to "10")
): Script {
    return Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG, idOrCode, emptyMap<String, String>(), params)
}

fun randomScript(source: String = "return " + Random().nextBoolean().toString()): Script = Script(source)

fun randomTemplateScript(
    source: String,
    params: Map<String, String> = emptyMap()
): Script = Script(ScriptType.INLINE, Script.DEFAULT_TEMPLATE_LANG, source, params)

fun randomAction(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    template: Script = randomTemplateScript("Hello World"),
    destinationId: String = "",
    throttleEnabled: Boolean = false,
    throttle: Throttle = randomThrottle()
) = Action(name, destinationId, template, template, throttleEnabled, throttle, actionExecutionPolicy = null)

fun randomActionWithPolicy(
    name: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    template: Script = randomTemplateScript("Hello World"),
    destinationId: String = "",
    throttleEnabled: Boolean = false,
    throttle: Throttle = randomThrottle(),
    actionExecutionPolicy: ActionExecutionPolicy? = randomActionExecutionPolicy()
): Action {
    return if (actionExecutionPolicy?.actionExecutionScope is PerExecutionActionScope) {
        // Return null for throttle when using PerExecutionActionScope since throttling is currently not supported for it
        Action(name, destinationId, template, template, throttleEnabled, null, actionExecutionPolicy = actionExecutionPolicy)
    } else {
        Action(name, destinationId, template, template, throttleEnabled, throttle, actionExecutionPolicy = actionExecutionPolicy)
    }
}

fun randomThrottle(
    value: Int = RandomNumbers.randomIntBetween(Random(), 60, 120),
    unit: ChronoUnit = ChronoUnit.MINUTES
) = Throttle(value, unit)

fun randomActionExecutionPolicy(
    actionExecutionScope: ActionExecutionScope = randomActionExecutionScope()
) = ActionExecutionPolicy(actionExecutionScope)

fun randomActionExecutionScope(): ActionExecutionScope {
    return if (Random().nextBoolean()) {
        val alertCategories = AlertCategory.values()
        PerAlertActionScope(actionableAlerts = (1..RandomNumbers.randomIntBetween(Random(), 0, alertCategories.size)).map { alertCategories[it - 1] }.toSet())
    } else {
        PerExecutionActionScope()
    }
}

fun randomDocLevelQuery(
    id: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    query: String = RandomStrings.randomAsciiLettersOfLength(Random(), 10),
    name: String = "${RandomNumbers.randomIntBetween(Random(), 0, 5)}",
    tags: List<String> = mutableListOf(0..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { RandomStrings.randomAsciiLettersOfLength(Random(), 10) }
): DocLevelQuery {
    return DocLevelQuery(id = id, query = query, name = name, tags = tags)
}

fun randomDocLevelMonitorInput(
    description: String = RandomStrings.randomAsciiLettersOfLength(Random(), RandomNumbers.randomIntBetween(Random(), 0, 10)),
    indices: List<String> = listOf(1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { RandomStrings.randomAsciiLettersOfLength(Random(), 10) },
    queries: List<DocLevelQuery> = listOf(1..RandomNumbers.randomIntBetween(Random(), 0, 10)).map { randomDocLevelQuery() }
): DocLevelMonitorInput {
    return DocLevelMonitorInput(description = description, indices = indices, queries = queries)
}

fun randomClusterMetricsInput(
    path: String = ClusterMetricsInput.ClusterMetricType.CLUSTER_HEALTH.defaultPath,
    pathParams: String = "",
    url: String = ""
): ClusterMetricsInput {
    return ClusterMetricsInput(path, pathParams, url)
}

fun Monitor.toJsonString(): String {
    val builder = XContentFactory.jsonBuilder()
    return this.toXContent(builder, ToXContent.EMPTY_PARAMS).string()
}

fun Monitor.toJsonStringWithUser(): String {
    val builder = XContentFactory.jsonBuilder()
    return this.toXContentWithUser(builder, ToXContent.EMPTY_PARAMS).string()
}

fun randomUser(): User {
    return User(
        RandomStrings.randomAsciiLettersOfLength(Random(), 10),
        listOf(
            RandomStrings.randomAsciiLettersOfLength(Random(), 10),
            RandomStrings.randomAsciiLettersOfLength(Random(), 10)
        ),
        listOf(RandomStrings.randomAsciiLettersOfLength(Random(), 10), ALL_ACCESS_ROLE),
        listOf("test_attr=test")
    )
}

fun randomUserEmpty(): User {
    return User("", listOf(), listOf(), listOf())
}

/**
 * Wrapper for [RestClient.performRequest] which was deprecated in ES 6.5 and is used in tests. This provides
 * a single place to suppress deprecation warnings. This will probably need further work when the API is removed entirely
 * but that's an exercise for another day.
 */
@Suppress("DEPRECATION")
fun RestClient.makeRequest(
    method: String,
    endpoint: String,
    params: Map<String, String> = emptyMap(),
    entity: HttpEntity? = null,
    vararg headers: Header
): Response {
    val request = Request(method, endpoint)
    // TODO: remove PERMISSIVE option after moving system index access to REST API call
    val options = RequestOptions.DEFAULT.toBuilder()
    options.setWarningsHandler(WarningsHandler.PERMISSIVE)
    headers.forEach { options.addHeader(it.name, it.value) }
    request.options = options.build()
    params.forEach { request.addParameter(it.key, it.value) }
    if (entity != null) {
        request.entity = entity
    }
    return performRequest(request)
}

/**
 * Wrapper for [RestClient.performRequest] which was deprecated in ES 6.5 and is used in tests. This provides
 * a single place to suppress deprecation warnings. This will probably need further work when the API is removed entirely
 * but that's an exercise for another day.
 */
@Suppress("DEPRECATION")
fun RestClient.makeRequest(
    method: String,
    endpoint: String,
    entity: HttpEntity? = null,
    vararg headers: Header
): Response {
    val request = Request(method, endpoint)
    val options = RequestOptions.DEFAULT.toBuilder()
    // TODO: remove PERMISSIVE option after moving system index access to REST API call
    options.setWarningsHandler(WarningsHandler.PERMISSIVE)
    headers.forEach { options.addHeader(it.name, it.value) }
    request.options = options.build()
    if (entity != null) {
        request.entity = entity
    }
    return performRequest(request)
}

fun builder(): XContentBuilder {
    return XContentBuilder.builder(XContentType.JSON.xContent())
}

fun parser(xc: String): XContentParser {
    val parser = XContentType.JSON.xContent().createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, xc)
    parser.nextToken()
    return parser
}

fun xContentRegistry(): NamedXContentRegistry {
    return NamedXContentRegistry(
        listOf(
            SearchInput.XCONTENT_REGISTRY,
            DocLevelMonitorInput.XCONTENT_REGISTRY,
            QueryLevelTrigger.XCONTENT_REGISTRY,
            BucketLevelTrigger.XCONTENT_REGISTRY,
            DocumentLevelTrigger.XCONTENT_REGISTRY
        ) + SearchModule(Settings.EMPTY, emptyList()).namedXContents
    )
}

fun assertUserNull(map: Map<String, Any?>) {
    val user = map["user"]
    assertNull("User is not null", user)
}

fun assertUserNull(monitor: Monitor) {
    assertNull("User is not null", monitor.user)
}
