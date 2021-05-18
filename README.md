# OpenSearch Common Utils 

OpenSearch Common Utils is focused on providing reusable Java components for OpenSearch plugins.

This library is composed of following parts:
1. SecureRestClientBuilder - provides methods to create secure low-level and high-level REST client. This is 
   useful to make secure REST calls to OpenSearch or other plugin api's. 
2. InjectSecurity - provides methods to inject user or roles. This is useful for running background jobs securely.
3. IntegTestsWithSecurity - provides methods to create users, roles for running integ tests with security plugin.
4. Shared Request/Response/Action classes used for plugin to plugin transport layer calls.
5. Any common functionality across OpenSearch plugins could be moved to this.


## Setup

1. Check out this package from version control.
1. Launch Intellij IDEA, choose **Import Project**, and select the `settings.gradle` file in the root of this package. 
1. To build from the command line, set `JAVA_HOME` to point to a JDK >= 14 before running `./gradlew`.


## Build

### Building from the command line
```
./gradlew clean
./gradlew build 

./gradlew publishToMavenLocal
```

### Logging

To change loglevel, add below to `config/log4j2.properties` or use REST API to set.
```
logger.commons.name = org.opensearch.commons
logger.commons.level = debug
```

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](https://opendistro.github.io/for-elasticsearch/codeofconduct.html).


## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.


## Licensing

See the [LICENSE](./LICENSE.txt) file for our project's licensing. We will ask you to confirm the licensing of your contribution.


## Copyright

Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.