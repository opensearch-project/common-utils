## Version 2.12.0.0 2023-02-06

Compatible with OpenSearch 2.12.0

### Maintenance
* Increment version to 2.12.0-SNAPSHOT ([#545](https://github.com/opensearch-project/common-utils/pull/545))
* Onboard prod jenkins docker image to github actions ([#557](https://github.com/opensearch-project/common-utils/pull/557))
* Update Gradle to 8.4 ([#560](https://github.com/opensearch-project/common-utils/pull/560))
* Add Java 11/17/21 matrix for build, test and integration checks ([#561](https://github.com/opensearch-project/common-utils/pull/561))
* changed all usages of 'admin' as a password to something different ([#581](https://github.com/opensearch-project/common-utils/pull/581))
* Update dependency com.pinterest:ktlint to 0.47.1 and fix CVE-2023-6378 ([#585](https://github.com/opensearch-project/common-utils/pull/585))

### Enhancement
* add 'fields' parameter in doc level query object. ([#546](https://github.com/opensearch-project/common-utils/pull/546))
* add fields param in toxcontent() for doc level query ([#549](https://github.com/opensearch-project/common-utils/pull/549))
* Add User.isAdminDn to User class ([#547](https://github.com/opensearch-project/common-utils/pull/547))

### Refactor
* Move get monitor and search monitor action / request / responses to common-utils ([#566](https://github.com/opensearch-project/common-utils/pull/566))

# Features
* Implemented cross-cluster monitor support ([#584](https://github.com/opensearch-project/common-utils/pull/584))

### Documentation
* Added 2.12.0.0 release notes ([#585](https://github.com/opensearch-project/common-utils/pull/585))