# Vaadin Simple Security — AGENTS.md

## What this is

A library providing simple security support for Vaadin. Doesn't use nor depend on Spring. Perfect for
your [Vaadin Boot](https://github.com/mvysny/vaadin-boot)-based projects.
Implements ideas behind the [Securing Plain Java Applications](https://vaadin.com/docs/latest/security/advanced-topics/securing-plain-java-app)
Vaadin documentation, but doesn't depend on the servlet container security configuration -
instead the users are managed directly by the application.

## Promises

- **No Spring, no Shiro.** A plain `main()` Vaadin app gets authentication and authorization without a security framework or a DI container.
- **The application owns its users.** Users and roles live wherever the app keeps them; never in servlet-container realm configuration.
- **No abstraction over authentication schemes.** The library holds the session and guards routes; how a user proves who they are is the app's own `login()`. See `D_no_security_framework`.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` | the pitch, install, the user-facing tutorial, the compatibility matrix | — |
| `CONTRIBUTING.md` | the release steps | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/architecture.md` | how the pieces compose — wiring, the navigation / login / external sign-in flows; normative | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| doc comments | what one symbol does and why it is shaped so | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **Vaadin and Jakarta are `compileOnly` in the published modules.** A bundled Vaadin clashes with the consumer's own version.
- **A login service is session-scoped, looked up through `AbstractLoginService.get(Class, Supplier)`.** A singleton would share one logged-in user across all sessions; the wiring is in architecture.md.
- **Everything stored in the session is `Serializable`**, suppliers included. Session persistence and replication fail otherwise.
- **An external token is validated server-side before any `login(...)` call.** A client-side check can be spoofed by any script on the page.

## Module map

- `vaadin-simple-security` — the library: login service base, navigation access control, password hashing, the `.inmemory` demo.
- `externalauth/google` — "Sign in with Google" button with server-side ID-token validation; published as `externalauth-google`.
- `testapp` — Vaadin Boot app wiring the library; its Karibu tests verify access control end to end.

## Conventions

- **Production code is Java 21; Kotlin only in the core module's tests.** `testapp` and `externalauth/google` tests are Java.
- **Tests: JUnit Jupiter + Karibu-Testing**, in-JVM without a browser; no mocking library.
- **Nullability is annotated** with JetBrains `@NotNull` / `@Nullable` on every public parameter and return value.
- **Dependency versions live in `gradle/libs.versions.toml`**, never in a module's `build.gradle.kts`.
- **`master` is 2.x; a fix that may be backported to `0.x` avoids Vaadin-25-only API.** The compatibility matrix is in README.

## Commands

- `./gradlew` — clean + build (the default tasks): all tests and `design/verify_design_tripwires.sh`; what CI runs on push and PR, JDK 21 and 25, plus the tripwire as a job of its own (`.github/workflows/gradle.yml`).
- `./gradlew :vaadin-simple-security:test`, `:testapp:test`, `:externalauth:google:test` — one module's tests.
- `./gradlew test --tests "*PasswordHashTest"` — one test class by pattern.
- `./gradlew :testapp:run` — the testapp in embedded Jetty.
- `./gradlew clean build publish closeAndReleaseStagingRepositories` — release to Maven Central; the full steps are in `CONTRIBUTING.md`.

## Skills this project follows

- **Karibu-Testing:** browserless Vaadin tests with `MockVaadin`, `_get` / `LocatorJ` lookups; the `karibu-testing` skill has the helpers.
- **Javadoc at the right level:** a fact about one symbol goes in its doc comment, not in prose here; the `writing-javadoc` skill has the rules.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its doc comment, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.
