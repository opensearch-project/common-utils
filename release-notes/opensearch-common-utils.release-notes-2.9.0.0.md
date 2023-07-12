## Version 2.9.0.0 2023-07-11

Compatible with OpenSearch 2.9.0

### Maintenance
* Increment version to 2.9.0-SNAPSHOT. ([#444](https://github.com/opensearch-project/common-utils/pull/444))
* Modify triggers to push snapshots on all branches. ([#454](https://github.com/opensearch-project/common-utils/pull/454))

### Feature
* Adds Chained alerts triggers for workflows. ([#456](https://github.com/opensearch-project/common-utils/pull/456))
* Acknowledge chained alert request for workflow. ([#459](https://github.com/opensearch-project/common-utils/pull/459))
* Adds audit state in Alert. ([#461](https://github.com/opensearch-project/common-utils/pull/461))
* Add workflowId field in alert. (([#463](https://github.com/opensearch-project/common-utils/pull/463))
* APIs for get workflow alerts and acknowledge chained alerts. ([#472](https://github.com/opensearch-project/common-utils/pull/472))
* Add auditDelegateMonitorAlerts flag. ([#476](https://github.com/opensearch-project/common-utils/pull/476))
* Implemented support for configuring a cluster metrics monitor to call cat/indices, and cat/shards. ([#479](https://github.com/opensearch-project/common-utils/pull/479))


### Bug Fixes
* OpenSearch commons strings library dependency import. ([#474](https://github.com/opensearch-project/common-utils/pull/474))

### Refactoring
* Pass workflow id in alert constructors. ([#465](https://github.com/opensearch-project/common-utils/pull/465))

### Documentation
* Added 2.9 release notes. ([#482](https://github.com/opensearch-project/common-utils/pull/482))