## Version 2.7.0.0 2023-04-17

Compatible with OpenSearch 2.7.0

### Maintenance
* Increment version to 2.7.0-SNAPSHOT. ([#371](https://github.com/opensearch-project/common-utils/pull/371))
  
### Refactoring
* Fixed xContent dependencies due to OSCore changes. ([#392](https://github.com/opensearch-project/common-utils/pull/392))

### Infrastructure
* Publish snapshots to maven via GHA. ([#365](https://github.com/opensearch-project/common-utils/pull/365))
* Add auto Github release workflow. ([#376](https://github.com/opensearch-project/common-utils/pull/376))

### Feature
* InjectSecurity - inject User object in UserInfo in threadContext. ([#396](https://github.com/opensearch-project/common-utils/pull/396))

### Bug Fixes
* Fix SNS regex for validation on notification channel to support SNS FIFO topics. ([#381](https://github.com/opensearch-project/common-utils/pull/381))

### Documentation
* Added 2.7 release notes. ([#407](https://github.com/opensearch-project/common-utils/pull/407))