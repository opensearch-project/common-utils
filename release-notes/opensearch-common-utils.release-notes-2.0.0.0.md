## Version 2.0.0.0 2022-05-18

Compatible with OpenSearch 2.0.0

### Enhancements

  * Add SQL/PPL transport request/response models for SQL plugin ([#155](https://github.com/opensearch-project/common-utils/pull/155))
  * Support sending email message via Notifications pass-through API ([#158](https://github.com/opensearch-project/common-utils/pull/158))

### Infrastructure

  * Upgrade gradle artifacts to 7.3.3 ([#135](https://github.com/opensearch-project/common-utils/pull/135)
  * Update common-utils to depend on the OpenSearch repositories plugin ([#137](https://github.com/opensearch-project/common-utils/pull/137))
  * Add sign-off option for version workflow PR ([#143](https://github.com/opensearch-project/common-utils/pull/143))
  * Add qualifier default to alpha1 in build.gradle ([#151](https://github.com/opensearch-project/common-utils/pull/151))
  * Update issue templates from github for bugs and features ([#154](https://github.com/opensearch-project/common-utils/pull/154))
  * Remove support for JDK 14 ([#159](https://github.com/opensearch-project/common-utils/pull/159))
  * Remove RC1 as the qualifier from Common Utils ([#168](https://github.com/opensearch-project/common-utils/pull/168))

### Refactoring

  * Remove feature and feature_list usage for Notifications ([#136](https://github.com/opensearch-project/common-utils/pull/136))
  * Rename references for Get Channels API for Notifications ([#140](https://github.com/opensearch-project/common-utils/pull/140))
  * Remove allowedConfigFeatureList from GetPluginFeaturesResponse for Notifications ([#144](https://github.com/opensearch-project/common-utils/pull/144))
  * Remove NotificationEvent Request, Response and SearchResults ([#153](https://github.com/opensearch-project/common-utils/pull/153))
  * Add NotificationEvent to SendNotificationResponse and Removal of NotificationID ([#156](https://github.com/opensearch-project/common-utils/pull/156))
  * Change BaseModel to extend ToXContentObject instead of ToXContent ([#173](https://github.com/opensearch-project/common-utils/pull/173))

### Documentation

  * Add release notes for version 2.0.0-rc1 ([#162](https://github.com/opensearch-project/common-utils/pull/162))
  * Add release notes for version 2.0.0.0 ([#177](https://github.com/opensearch-project/common-utils/pull/177))
