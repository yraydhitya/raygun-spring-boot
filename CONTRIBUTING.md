# raygun-spring-boot

## Setup

This project uses [google-java-format](https://github.com/google/google-java-format) through [Spotless plugin for Gradle](https://github.com/diffplug/spotless/tree/main/plugin-gradle). Please install the plugin in your IDE.

## Format

To check whether the current code comply with the formatter or not:

```sh
./gradlew raygun-spring-boot-starter:spotlessJavaCheck
```

The format check is run on every compile.

To apply the formatter:

```sh
./gradlew raygun-spring-boot-starter:spotlessJavaApply
```

## Test

```sh
./gradlew raygun-spring-boot-starter:test
```

## Code Coverage Verification

```sh
./gradlew raygun-spring-boot-starter:test raygun-spring-boot-starter:jacocoTestReport raygun-spring-boot-starter:jacocoTestCoverageVerification
```

## Javadoc

Public API that is intended to be used in user's code must have Javadoc.
