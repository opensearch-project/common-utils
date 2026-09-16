## Version 3.9.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.9.0

### Enhancements

* Override `DocRequest.type()` on alerting request classes and tolerate ancillary top-level fields in `ScheduledJob.parse` for resource-sharing framework support ([#981](https://github.com/opensearch-project/common-utils/pull/981))

### Bug Fixes

* Fix `ScheduledJob.parse` type overload broken by shared-parse refactor, restoring correct parser position handling for the sweeper path ([#984](https://github.com/opensearch-project/common-utils/pull/984))
* Align `WORKFLOW_RESOURCE_TYPE` constant with alerting's registered resource type (`alerting-workflow`) to ensure proper security gating of workflow requests ([#1003](https://github.com/opensearch-project/common-utils/pull/1003))
