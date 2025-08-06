## Version 3.2.0 Release Notes

Compatible with OpenSearch and OpenSearch Dashboards version 3.2.0

### Features
* Add Seconds as a supported unit for IntervalSchedule ([#849](https://github.com/opensearch-project/common-utils/pull/849))

### Enhancements
* Add tenancy access info to serialized user in threadcontext ([#857](https://github.com/opensearch-project/common-utils/pull/857))

### Bug Fixes
* Pinned the commons-beanutils dependency to fix CVE-2025-48734 ([#850](https://github.com/opensearch-project/common-utils/pull/850))
* Revert "updating PublishFindingsRequest to use a list of findings rather than… ([#847](https://github.com/opensearch-project/common-utils/pull/847))

### Infrastructure
* Switch gradle to 8.14 and JDK to 24 ([#848](https://github.com/opensearch-project/common-utils/pull/848))
* Update Maven snapshots publishing endpoint and credential retrieval ([#841](https://github.com/opensearch-project/common-utils/pull/841))