# Contributing

Thank you so much for making the library better.
Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

## Tests

Uses [DynaTest](https://github.com/mvysny/dynatest). Simply run `./gradlew test` to run all tests.

Running `./gradlew` or `./gradlew test` will run all tests on all databases (given that
Docker is available on the host system). To run the tests on H2 only
(the test suite will run much faster), run with `./gradlew -Dh2only=true`

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza
2. Commit with the commit message of simply being the version being released, e.g. "1.2.13"
3. git tag the commit with the same tag name as the commit message above, e.g. `1.2.13`
4. `git push`, `git push --tags`
5. Run `./gradlew clean build publish`
6. Continue to the [OSSRH Nexus](https://oss.sonatype.org/#stagingRepositories) and follow the [release procedure](https://central.sonatype.org/pages/releasing-the-deployment.html).
7. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 1.2.14-SNAPSHOT, then commit with the commit message "1.2.14-SNAPSHOT" and push.
