## Version 2.19.3.0 2025-07-21

Compatible with OpenSearch 2.19.3

### Maintenance
* Increment version to 2.19.3-SNAPSHOT ([#826](https://github.com/opensearch-project/common-utils/pull/826))
* Pinned the commons-beanutils dependency to fix CVE-2025-48734 ([#850](https://github.com/opensearch-project/common-utils/pull/850))

## Bug fix
* validate that index patterns are not allowed in create/update doc level monitor ([#829](https://github.com/opensearch-project/common-utils/pull/829))
* Fix isDocLevelMonitor check to account for threat intel monitor ([#835](https://github.com/opensearch-project/common-utils/pull/835))
* updating PublishFindingsRequest to use a list of findings rather than... ([#832](https://github.com/opensearch-project/common-utils/pull/832))
* Revert "updating PublishFindingsRequest to use a list of findings" ([#842](https://github.com/opensearch-project/common-utils/pull/842))

### Documentation
* Added 2.19.3.0 release notes. ([#854](https://github.com/opensearch-project/common-utils/pull/854))