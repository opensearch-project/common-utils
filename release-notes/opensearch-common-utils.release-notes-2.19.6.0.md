## Version 2.19.6 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 2.19.6

### Bug Fixes

* Validate api_type matches path in ClusterMetricsInput to prevent creating monitors with mismatched fields that cannot be deleted ([#914](https://github.com/opensearch-project/common-utils/pull/914))
* Normalize cluster metrics input URI path during validation to fix exception when path is not prepended with `/` ([#922](https://github.com/opensearch-project/common-utils/pull/922))

### Infrastructure

* Add CI mirror repository to avoid Maven Central throttling during builds ([#976](https://github.com/opensearch-project/common-utils/pull/976))
