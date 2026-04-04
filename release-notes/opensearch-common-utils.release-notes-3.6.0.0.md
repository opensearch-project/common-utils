## Version 3.6.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.6.0

### Features

* Add Target object for external data source support on Monitor and Alert models ([#916](https://github.com/opensearch-project/common-utils/pull/916))

### Enhancements

* Remove hardcoded trigger limit from Monitor data class and make trigger count per monitor configurable ([#913](https://github.com/opensearch-project/common-utils/pull/913))
* Validate that api_type matches path in ClusterMetricsInput to prevent mismatched monitor configurations ([#912](https://github.com/opensearch-project/common-utils/pull/912))

### Bug Fixes

* Normalize cluster metrics input URI path during validation to fix exception when path lacks leading slash ([#921](https://github.com/opensearch-project/common-utils/pull/921))
* Revert addition of Target object for external data source support on Monitor and Alert models ([#917](https://github.com/opensearch-project/common-utils/pull/917))

### Infrastructure

* Update shadow plugin usage to replace deprecated API in preparation for dependency upgrade ([#904](https://github.com/opensearch-project/common-utils/pull/904))

### Maintenance

* Bump logback from 1.5.19 to 1.5.32 ([#907](https://github.com/opensearch-project/common-utils/pull/907))
