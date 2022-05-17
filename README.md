[![codecov](https://codecov.io/gh/opensearch-project/common-utils/branch/main/graph/badge.svg)](https://codecov.io/gh/opensearch-project/common-utils)

<img src="https://opensearch.org/assets/img/opensearch-logo-themed.svg" height="64px">

- [OpenSearch Common Utils](#opensearch-common-utils)
- [Contributing](#contributing)
- [Getting Help](#getting-help)
- [Code of Conduct](#code-of-conduct)
- [Security](#security)
- [License](#license)
- [Copyright](#copyright)

## OpenSearch Common Utils 

OpenSearch Common Utils is focused on providing reusable Java components for [OpenSearch](https://opensearch.org/) plugins.

This library is composed of following parts:

1. `SecureRestClientBuilder` - provides methods to create secure low-level and high-level REST client. This is useful to make secure REST calls to OpenSearch or other plugin APIs. 
2. `InjectSecurity` - provides methods to inject user or roles. This is useful for running background jobs securely.
3. `IntegTestsWithSecurity` - provides methods to create users, roles for running integration tests with security plugin.
4. Shared request/response/action classes used for plugin to plugin transport layer calls.
5. Any common functionality across OpenSearch plugins could be moved to this.

## Contributing

See [developer guide](DEVELOPER_GUIDE.md) and [how to contribute to this project](CONTRIBUTING.md). 

## Getting Help

If you find a bug, or have a feature request, please don't hesitate to open an issue in this repository.

For more information, see [project website](https://opensearch.org/) and [documentation](https://docs-beta.opensearch.org/). If you need help and are unsure where to open an issue, try [forums](https://discuss.opendistrocommunity.dev/).

## Code of Conduct

This project has adopted the [Amazon Open Source Code of Conduct](CODE_OF_CONDUCT.md). For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq), or contact [opensource-codeofconduct@amazon.com](mailto:opensource-codeofconduct@amazon.com) with any additional questions or comments.

## Security

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

This project is licensed under the [Apache v2.0 License](LICENSE.txt).

## Copyright

Copyright OpenSearch Contributors. See [NOTICE](NOTICE.txt) for details.
