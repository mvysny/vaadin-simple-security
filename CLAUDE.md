# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A library that adds authentication + authorization to Vaadin Flow apps **without** Spring or Shiro. Targets Vaadin Boot-style apps (plain `main()` with embedded Jetty). Current development version is `2.0-SNAPSHOT`, targeting Vaadin 25+ and JDK 21+.

The library is published to Maven Central under `com.github.mvysny.vaadin-simple-security`.

## Build & Test Commands

All commands run from the repo root; the project uses the Gradle wrapper.

```bash
./gradlew build                              # clean + build all modules (defaultTasks)
./gradlew :vaadin-simple-security:test       # run tests of core module only
./gradlew :testapp:test                      # run testapp (Karibu) tests
./gradlew :externalauth:google:test          # run google auth module tests
./gradlew test --tests "*PasswordHashTest"   # run a single test class by pattern
./gradlew :testapp:run                       # launch the testapp in embedded Jetty
./gradlew clean build publish                # release to OSSRH (see CONTRIBUTING.md)
```

Tests use JUnit 6 Jupiter (`useJUnitPlatform()`); test sources are written in Kotlin (the core module keeps Kotlin as a *test-only* dependency — production code is strictly Java). Karibu-Testing drives the Vaadin UI tests without a browser.

## Module Layout

Gradle multi-module project (see `settings.gradle.kts`):

- **`vaadin-simple-security/`** — the library itself. Depends on Vaadin / Jakarta only via `compileOnly` so consumers bring their own.
- **`testapp/`** — a Vaadin Boot app used as an integration-test harness. `Main.java` runs `new VaadinBoot().run()`. Tests under `testapp/src/test` use Karibu to navigate routes and verify access control end-to-end.
- **`externalauth/google/`** — optional module providing `GoogleSignInButton` (Google Identity / "Sign in with Google"). Published separately as `externalauth-google`.

Version catalog lives in `gradle/libs.versions.toml` — bump Vaadin / SLF4J / Karibu versions there, not in individual `build.gradle.kts` files.

## Core Architecture

The library wires three collaborating pieces together. Understanding how they connect is the key to being productive here:

1. **`AbstractLoginService<U>`** — a **session-scoped** service (stored in `VaadinSession`) that holds the currently logged-in user and exposes `login(user)`, `logout()`, `getCurrentUser()`, `getCurrentPrincipal()`. Subclasses implement `toUserWithRoles(U)` to bridge an app-specific user type (e.g. JDBI entity) to the library's `SimpleUserWithRoles` principal. Subclasses are expected to expose a `static get()` helper that delegates to `AbstractLoginService.get(Class, Supplier)` for session lookup + lazy init.

2. **`SimpleNavigationAccessControl`** — extends Vaadin's `NavigationAccessControl`. Consumers install it as a `BeforeEnterListener` on every UI via a `VaadinServiceInitListener`. It reads the current principal from a caller-provided `Supplier<SimpleUserWithRoles>` (usually `MyLoginService::get`) instead of from `HttpServletRequest`. Honors standard Vaadin annotations `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed`, `@DenyAll`.

3. **Built-in implementations** under `.inmemory` — `InMemoryUserRegistry` (global user store), `InMemoryUser`, `InMemoryLoginService`. These are demo-grade; production apps substitute a SQL-backed `AbstractLoginService`.

Supporting utilities:
- **`HasPassword`** — mixin interface for entity classes. Combined with `PasswordHash` (PBKDF2 in `util/`) to do password hashing + salting + verification.
- **`DirectLoginService`** — concrete `AbstractLoginService` for apps where authentication happens *entirely* externally (Google SSO, LDAP, etc.) and there's no local user table to look up. Call `.login(username, roles)` after the external token is validated.
- **`AccessRejectedException`** — thrown when a logged-in user hits a route their roles don't cover.

### Typical wiring (see `testapp/src/main/java/com/example/security/ApplicationServiceInitListener.java` for a working example)

A `VaadinServiceInitListener` constructs the `SimpleNavigationAccessControl`, calls `setLoginView(LoginRoute.class)`, and in `serviceInit()` attaches the control as a `BeforeEnterListener` on every new UI. The login route calls `MyLoginService.get().login(username, password)` which on success stores the user in the session; subsequent navigations see the principal via `SimpleNavigationAccessControl.getPrincipal()`.

### External auth flow

The flow for OAuth-style providers (`externalauth/google/` is the reference implementation): a client-side widget collects a provider token, ships it to the server via Vaadin RPC, the server validates the token's signature, then calls either `DirectLoginService.login(...)` (no local users) or a custom `loginDirectly(...)` on the app's `AbstractLoginService` (hybrid: external auth + local user table). Never trust the token client-side — always validate server-side.

## Release Process

See `CONTRIBUTING.md`. Summary: drop `-SNAPSHOT` in `build.gradle.kts`, commit with version as message, tag, push, `./gradlew clean build publish`, finish on OSSRH Nexus, then bump to next `-SNAPSHOT`.

## Compatibility

- `2.x` (current) → Vaadin 25+, JDK 21+
- `1.x` → Vaadin 24.3+, JDK 17+
- `0.x` (branch `0.x`) → Vaadin 23+, JDK 11+

When touching APIs, respect these boundaries — don't introduce Vaadin-25-only usages into files that might be backported.
