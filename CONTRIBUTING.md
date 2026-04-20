# Contributing

Thank you so much for making the library better.
Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

## Tests

Uses JUnit 6 Jupiter, with [Karibu-Testing](https://github.com/mvysny/karibu-testing)
driving Vaadin UI tests in-JVM (no browser needed). Run `./gradlew build` to
build and test everything, or `./gradlew test` to only run tests.

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza, e.g. "2.0"
2. Edit `README.md` and `externalauth/google/README.md`: bump the version in the Gradle install snippets to match the release
3. Run `./gradlew clean build publish closeAndReleaseStagingRepositories`
4. (Optional) watch [Maven Central Publishing Deployments](https://central.sonatype.com/publishing/deployments) as the deployment is published.
5. Commit with the commit message of simply being the version being released, e.g. "2.0"
6. git tag the commit with the same tag name as the commit message above, e.g. `2.0`
7. `git push`, `git push --tags`
8. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 2.1-SNAPSHOT, then commit with the commit message "2.1-SNAPSHOT" and push.
