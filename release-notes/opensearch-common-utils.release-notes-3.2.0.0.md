## Version 3.2.0.0 2025-08-04

Compatible with OpenSearch 3.2.0

### Enhancements
* Add Seconds as a supported unit for IntervalSchedule ([#849](https://github.com/opensearch-project/common-utils/pull/849))
* Add tenancy access info to serialized user in threadcontext ([#857](https://github.com/opensearch-project/common-utils/pull/857))

### Maintenance
* Increment version to 3.2.0-SNAPSHOT ([#844](https://github.com/opensearch-project/common-utils/pull/844))
* Update Maven snapshots publishing endpoint and credential retrieval ([#841](https://github.com/opensearch-project/common-utils/pull/841))
* Switch gradle to 8.14 and JDK to 24 ([#848](https://github.com/opensearch-project/common-utils/pull/848))
* Pinned the commons-beanutils dependency to fix CVE-2025-48734 ([#850](https://github.com/opensearch-project/common-utils/pull/850))

### Bug fixes
* Revert "updating PublishFindingsRequest to use a list of findings rather than...(#832)" ([#847](https://github.com/opensearch-project/common-utils/pull/847))