## Version 2.4.0.0 2022-11-04

Compatible with OpenSearch 2.4.0

### Infrastructure
* fix snakeyaml vulnerability issue by disabling detekt([#237](https://github.com/opensearch-project/common-utils/pull/237))
* upgrade 2.x to 2.4 ([#246](https://github.com/opensearch-project/common-utils/pull/246))
* remove force snakeyaml removal ([#263](https://github.com/opensearch-project/common-utils/pull/263))

### Refactoring
* Move Alerting data models over to common-utils ([#242](https://github.com/opensearch-project/common-utils/pull/242))
* Copy over monitor datasources config from alerting to common utils ([#247](https://github.com/opensearch-project/common-utils/pull/247))
* expose delete monitor api from alerting ([#251](https://github.com/opensearch-project/common-utils/pull/251))
* Move Findings and Alerts action, request, response and models from alerting to common-utils ([#254](https://github.com/opensearch-project/common-utils/pull/254))
* Move acknowledge alerts dtos from alerting to common-utils ([#283](https://github.com/opensearch-project/common-utils/pull/282))

### Enhancements
* Accept of list of monitor ids in findings and alerts request dtos ([#277](https://github.com/opensearch-project/common-utils/pull/277))
* Added legacy support for SNS messages. ([#269](https://github.com/opensearch-project/common-utils/pull/269))
* add list of alert ids in get alerts request  ([#284](https://github.com/opensearch-project/common-utils/pull/284))
* fix security-analytics alerting findings api integration ([#292](https://github.com/opensearch-project/common-utils/pull/292))
* added params to Datasources ([#290](https://github.com/opensearch-project/common-utils/pull/290))
* fix security-analytics to alerting integration ([#293](https://github.com/opensearch-project/common-utils/pull/293))
* add findings enabled flag and findings field in bucket level monitor ([#305](https://github.com/opensearch-project/common-utils/pull/305))
* Support backend roles in indexMonitorRequest ([#308](https://github.com/opensearch-project/common-utils/pull/308))
* Added function for request recreation that considers the writeable request ([#303](https://github.com/opensearch-project/common-utils/pull/303))
* Adds owner field in monitor model ([#313](https://github.com/opensearch-project/common-utils/pull/313))

### Documentation
* Added 2.4 release notes. ([#316](https://github.com/opensearch-project/common-utils/pull/316))