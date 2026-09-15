# Architecture

How the pieces compose — what no single symbol can say and what would be expensive to overturn:
wiring and dependency direction, the lifecycle / threading / data-flow story, the flows a newcomer
needs, where to start reading. **Normative: the code conforms.** Change this file first, then the
code. Not here: why (`decisions.md` — cite the `D_`), what upstream does (`research.md` — cite the
`R_`), one symbol's behaviour (its doc comment), the module map (`AGENTS.md`). Cap 12 KB — over
it, research or doc-comment content has crept in.

---

## Wiring

- Dependencies point from the app into the library. `SimpleNavigationAccessControl` knows only a `SerializableSupplier<SimpleUserWithRoles>`; `usingService(...)` adapts any `AbstractLoginService` to it, so the core never references a concrete service or user type (`D_no_security_framework`).
- The app's `VaadinServiceInitListener` (registered in `META-INF/services`) constructs one access control, sets its login view, and adds it as a `BeforeEnterListener` to every UI through a `UIInitListener`.
- The login service lives in `VaadinSession` under its own class. It holds the app's user `U` and the principal derived from it by `toUserWithRoles` — the principal is all the access control ever sees.
- `.inmemory` is a consumer of the core API — registry, user, service — and the core depends on it only through a javadoc link.
- `externalauth/google` depends on the core, never the reverse. `GoogleSignInButton` validates the token and fires `OnSignInEvent`; the app's listener picks the login service.
- `testapp` is the end-to-end seam: real routes and the real init listener under Karibu `MockVaadin`; the core module tests each class in isolation.

## Flows

**A navigation** (UI thread, session locked):

1. The router fires `BeforeEnterEvent` → `NavigationAccessControl.beforeEnter`.
2. It asks `SimpleNavigationAccessControl.getPrincipal` / `getRolesChecker` → the supplier → `MyLoginService.get().getCurrentPrincipal()`.
3. Vaadin's `AnnotatedViewAccessChecker` checks the target route's annotations against that principal.
4. Allowed → the navigation proceeds. No principal → reroute to the login view. Principal without the role → access denied.

**A login** (UI thread):

1. The login route calls the app's `login(username, password)`; it loads the user and verifies it via `HasPassword.passwordMatches`, or throws `FailedLoginException`.
2. `AbstractLoginService.login(U)` stores the user and its principal, rotates the session id and navigates to `mainRoutePath` — a navigation, now with a principal.

**A logout:** `logout()` discards both sessions and reloads the page; the new UI has no principal, so the navigation reroutes to the login view.

**An external sign-in** (Google):

1. `google-signin-button.js` renders Google's button; Google's script hands it an ID token.
2. The token reaches `GoogleSignInButton` through a `@ClientCallable` — Vaadin RPC, so the session lock is held.
3. The server verifies the token's signature and audience with google-api-client; a failure is an `OnSignInEvent` carrying it.
4. The app's sign-in listener checks the e-mail (its domain, typically) and calls `DirectLoginService.get().login(email, roles)`, or its own `loginDirectly(...)` against a local user table.
5. From there it is the login flow, step 2.

## Where to start reading

`testapp/src/main/java/com/example/security/ApplicationServiceInitListener.java` — twenty lines of the whole wiring; then `SimpleNavigationAccessControl` and `AbstractLoginService`.
