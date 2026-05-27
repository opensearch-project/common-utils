## Version 3.7.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.7.0

### Features

* Add ScheduleTranslator and MonitorPayloadBuilder for external monitor scheduling ([#939](https://github.com/opensearch-project/common-utils/pull/939))
* Add interface for SqsAccountIdProvider to retrieve account IDs for SQS job queues ([#942](https://github.com/opensearch-project/common-utils/pull/942))
* Add ScheduleJobPayload model for serializing externally scheduled monitor job payloads ([#943](https://github.com/opensearch-project/common-utils/pull/943))
* Add Target object for external data source support on Monitor and Alert models ([#941](https://github.com/opensearch-project/common-utils/pull/941))
* Add ARN field to Target for tenant header propagation to remote resources ([#956](https://github.com/opensearch-project/common-utils/pull/956))
* Add PPL-related models to support PPL Alerting behind existing Alerting APIs ([#940](https://github.com/opensearch-project/common-utils/pull/940))
* Add TenantContext extension for preserving tenancy context across coroutines ([#952](https://github.com/opensearch-project/common-utils/pull/952))
* Add helper to preserve tenant ID header across SecureClientWrapper thread context stash ([#953](https://github.com/opensearch-project/common-utils/pull/953))
* Expose bucketsPathsMap in BucketSelectorExtAggregationBuilder for remote trigger evaluation ([#949](https://github.com/opensearch-project/common-utils/pull/949))
* Add isIndexNotFoundException utility to AlertingException for SDK migration support ([#934](https://github.com/opensearch-project/common-utils/pull/934))

### Enhancements

* Hide metadata field from REST API responses using secure flag ([#948](https://github.com/opensearch-project/common-utils/pull/948))
* Onboard code diff analyzer/reviewer and issue dedupe workflows ([#946](https://github.com/opensearch-project/common-utils/pull/946))
* Change max PPL Monitor name length from 30 to 100 ([#962](https://github.com/opensearch-project/common-utils/pull/962))

### Bug Fixes

* Change ScheduleJobPayload.jobStartTime from Instant to String to support both placeholders and timestamps ([#944](https://github.com/opensearch-project/common-utils/pull/944))

### Infrastructure

* Add Maven cache mirror before mavenCentral to reduce 429 throttling errors in CI builds ([#957](https://github.com/opensearch-project/common-utils/pull/957))
* Add issues write permission to untriaged label workflow ([#959](https://github.com/opensearch-project/common-utils/pull/959))
* Pin GitHub Actions to commit SHAs for supply chain security ([#961](https://github.com/opensearch-project/common-utils/pull/961))
* Pin actions/github-script to exact commit SHA ([#960](https://github.com/opensearch-project/common-utils/pull/960))

### Maintenance

* Baselined maintainers list ([#936](https://github.com/opensearch-project/common-utils/pull/936))
* Cleanup SafeSerializationUtils to remove unused Guava classes and add deserialization depth limit ([#958](https://github.com/opensearch-project/common-utils/pull/958))
* Remove unused SchedulePayloadBuilder superseded by ScheduleJobPayload ([#945](https://github.com/opensearch-project/common-utils/pull/945))
