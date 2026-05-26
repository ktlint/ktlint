First off, thanks for taking the time to contribute! This guide will answer some common questions about how this project works.

While this is a Pinterest open source project, we welcome contributions from everyone. Regular outside contributors can become project maintainers.

## Help

If you're having trouble using this project, please start by reading all documentation and searching for solutions in the existing open and closed issues.

## Security

If you've found a security issue in one of our open source projects, please report it at [Bugcrowd](https://bugcrowd.com/pinterest); you may even make some money!

## Code of Conduct

Please be sure to read and understand our [code of conduct](code-of-conduct.md). We work hard to ensure that our projects are welcoming and inclusive to as many people as possible.

## Reporting Issues

If you have a bug report, please provide as much information as possible so that we can help you out:

- Version of the project you're using.
- Code (or even better a sample project) which reproduce the issue.
- Steps which reproduce the issue.
- Stack traces for crashes.
- Any logs produced.

## Making Changes

!!! tip
    `ktlint` only provides rules that enforce the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) or [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide). If your change is more opinionated than please [file an issue](https://github.com/ktlint/ktlint/issues/new) first so that it can be discussed amongst the community. Rules which are too opinionated might be better published as a custom rule set. 

1. Fork this repository to your own account
2. Make your changes and verify that tests pass
3. Commit your work and push to a new branch on your fork
4. Submit a pull request
5. Participate in the code review process by responding to feedback

Once there is agreement that the code is in good shape, one of the project's maintainers will merge your contribution.

To increase the chances that your pull request will be accepted:

- Follow the coding style
- Write tests for your changes
- Write a good commit message
- Provide context in the pull request description.

New rules have to implement the `Rule.Experimental` interface so that the rule will only be run for user who have opted in to use experimental rules. Once the rule is stable, the marker interface `Rule.Experimental` can be removed.

## Updating dependencies

This project has enabled [Gradle dependencies verification](https://docs.gradle.org/6.2/userguide/dependency_verification.html). On adding/updating any dependency, ensure that you've added dependency provided checksum/signature to `gradle/verification-metadata.xml` file.

## Using kotlin development versions

Add following flag - `-PkotlinDev` to enable kotlin development version.

## License

By contributing to this project, you agree that your contributions will be licensed under its [license](../index.md#legal).
