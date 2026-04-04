## Version 3.6.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.6.0

### Features

* Add Target object for external data source support on Monitor and Alert models ([#916](https://github.com/opensearch-project/common-utils/pull/916))

### Enhancements

* Validate that api_type matches path in ClusterMetricsInput to prevent mismatched monitor configurations ([#912](https://github.com/opensearch-project/common-utils/pull/912))
* Remove hardcoded trigger limit from Monitor data class to allow configurable trigger counts via cluster setting ([#913](https://github.com/opensearch-project/common-utils/pull/913))

### Bug Fixes

* Normalize cluster metrics input URI path during validation to fix exception when path is not prepended with `/` ([#921](https://github.com/opensearch-project/common-utils/pull/921))
* Revert addition of Target object for external data source support on Monitor and Alert models ([#917](https://github.com/opensearch-project/common-utils/pull/917))

### Infrastructure

* Update shadow plugin usage to replace deprecated API in preparation for dependency update ([#904](https://github.com/opensearch-project/common-utils/pull/904))

### Maintenance

* Bump logback from 1.5.19 to 1.5.32 ([#907](https://github.com/opensearch-project/common-utils/pull/907))
