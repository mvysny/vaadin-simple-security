# Where an access check belongs — and should we still tell users to throw `AccessRejectedException`?

Started from "the class has zero usages"; ended up somewhere more interesting.

## What Vaadin actually gives you

Verified against flow-server 25.2.7 source, and by probing with Karibu (2026-09-16).

**Ordering, from `AbstractNavigationStateRenderer`:**

1. UI-level `BeforeEnterListener`s fire — this is where `SimpleNavigationAccessControl` sits.
   Nothing is constructed yet.
2. `sendBeforeEnterEventAndPopulateChain` walks the chain: for each element `getRouteTarget(..)`
   **constructs it**, then fires that element's own `beforeEnter` (and
   `HasUrlParameter.setParameter`).
3. `afterNavigation` last.

So the route's constructor has already run by the time its own `beforeEnter` /
`setParameter` gets the chance to reroute. Only the UI-level listener (step 1) prevents
construction at all.

**Escape hatches, by hook:**

| Hook | Has `BeforeEvent`? | Can reroute? | Aborts your method? |
|---|---|---|---|
| UI `BeforeEnterListener` | yes | `forwardTo` / `rerouteTo` / `rerouteToError` | n/a — nothing built yet |
| route `beforeEnter`, `HasUrlParameter.setParameter` | yes | same | **no** — you must `return` yourself |
| route constructor | no | — | throwing only |
| `afterNavigation` | `AfterNavigationEvent`: getters only | **none**, by design | throwing only |
| click listener, service below the UI | no | — | throwing only |

`rerouteTo` only flags the event; it is cooperative, not an abort. That's the gap
`AccessRejectedException` fills: **throwing is the feature**, the message/route/roles are cargo.

**Where a throw lands** (probed, both cases end on our own
`HasErrorParameter<AccessRejectedException>` view):

- from the **constructor**: `ReflectTools.createInstance` wraps it in
  `IllegalArgumentException("Unable to create an instance of 'X'. The constructor threw an
  exception.")`, but `getErrorNavigationTarget` calls `searchByCause` before `searchBySuperType`,
  so our view still wins. Caveat: in the error view `parameter.getCustomMessage()` is then the
  *wrapper's* text — the real one is `parameter.getException().getMessage()`.
- from **`afterNavigation`**: no wrapping; `getException()`, `getCaughtException()` and
  `getCustomMessage()` all carry ours.
- with **no** app-provided error view: Vaadin's `RouteAccessDeniedError` takes it (it handles the
  `AccessDeniedException` supertype) and rewrites it to a plain **404**, deliberately — a denied
  route must look like a missing one (flow#18870).

## Why `afterNavigation` has no reroute — intentional, not an omission

Dug through flow-server 25.2.7 sources and the tracker (2026-09-16). It is a **guarantee sold to
the listener**, stated the day the event landed.

**Mechanically**, `AbstractNavigationStateRenderer.handle()`:

```java
247  ui.getInternals().showRouteTarget(event.getLocation(), componentInstance, routerLayouts);
250  int statusCode = locationChangeEvent.getStatusCode();
251  validateStatusCode(statusCode, routeTargetType);
253  // After navigation event
254  handleAfterNavigationEvents(ui, parameters);
```

DOM committed at 247, status code captured at 250 — both *before* the event fires at 254. There is
nothing a flag could still influence; `handle()` returns that `statusCode` whatever listeners do.

**By design**, flow#2347 (Sep 2017) asked for the event with exactly one use case: highlight the
active menu item. In the review of flow#2433 the tutorial paragraph was rewritten to: *"When this
method is triggered, it is guaranteed that there will be no further redirects, so you can safely use
the location returned by the `AfterNavigationEvent`."* That sentence still stands in today's docs —
"the third and last event… further reroutes and similar changes are no longer possible".

**The vestige, and it is a trap.** The pre-Flow router had this hook *with* reroute: hummingbird#688
"Let the activated view trigger the 404 handler" is our use case verbatim. Its PR flow#1175 first
used a `RerouteException`, until Artur's review killed it ("*maybe there is some truth after all in
'don't use exceptions for control flow'*") in favour of `LocationChangeEvent.rerouteTo(..)` —
legitimate then, because `View.onLocationChange` ran *before* rendering. The 1.0 split moved reroute
to `BeforeEvent` and demoted `LocationChangeEvent` to a data carrier, **but never removed the
methods**. So this compiles today and silently does nothing:

```java
public void afterNavigation(AfterNavigationEvent e) {
    e.getLocationChangeEvent().rerouteTo(SomeView.class); // inert
}
```

`LocationChangeEvent.getRerouteTarget()` has no caller in flow-server — verified in the 25.2.7
sources and by GitHub code search on `main`; the only production `getRerouteTarget()` call is
`BeforeEvent`'s, in `AbstractNavigationStateRenderer.reroute(..)`. `setStatusCode` is likewise inert
from `afterNavigation`, being read at line 250.

**`ui.navigate(..)` from `afterNavigation`** is permitted — `Router.handleNavigationForLocation`
only blocks re-entry to the *same* path, so a nested navigation to a different one runs. Still worse
than throwing for a security check: the denied view was constructed, attached and shown, and the
outer `handle()` returns the *original* route's status code.

**Caveat on throwing** (flow#22146): a throw from a **layout**'s `afterNavigation`, with the error
view `@ParentLayout(ThatLayout.class)`, re-instantiates the layout for the error view, throws again,
escapes `ErrorStateRenderer` and kills navigation permanently. Fixed by flow#23177 (24.9.10 /
25.0.4 / 25.1.0-alpha3) with a fallback to `InternalServerError`. A throw from a *route*'s
`afterNavigation` — what we recommend — never hit it.

No open feature request asks for reroute in `AfterNavigationEvent`; of the 27 issues mentioning
`afterNavigation`, every one is about firing at the wrong time or losing parameters.

## The case-by-case tree

- **User may not see the route at all** → `@RolesAllowed` on the class. Cheapest and the only
  option that constructs nothing. No exception involved.
- **Route opens one DB entry the user may not read, id in the URL** → `setParameter`, with
  `event.rerouteToError(..)` / `forwardTo(..)` + `return`. Vaadin-idiomatic. Note this does *not*
  "skip init": the constructor already ran. It only works cleanly if the constructor builds the
  empty shell and `setParameter` loads the data — which is the Vaadin idiom anyway.
- **The input isn't in the URL** — a header ComboBox writes a selection into the session, the
  route reads it eagerly. This is the interesting one, and the reason the class exists.

## The trap that decides it (probed, not theory)

Navigating to the same route three times, with a detour in between:

```
ctor = 2     beforeEnter = 3     afterNavigation = 3
```

Two consecutive navigations to the same route **reuse the instance** — the constructor does not
run again (`sendBeforeEnterEventToExistingChain`). So:

> **A security check in a constructor runs once per instance, not once per navigation.**

For the ComboBox case that is exactly wrong: change the selection, navigate to the same route
again, and the constructor check is skipped. `beforeEnter` and `afterNavigation` run every time.

## Where that leaves us

- The *aesthetic* argument for throwing in the constructor is real — one `throw`, no
  half-initialized route, no "load the data then decide to discard it".
- The *correctness* argument is against it: re-entry skips it. A constructor is the wrong place
  for a check whose inputs can change between navigations, however clean it looks.
- Which leaves the honest recommendation: **check where the data arrives, not where the component
  is built** — `beforeEnter` for a route with a `BeforeEvent`, `afterNavigation` when there is
  none, the constructor never. And there, in `afterNavigation`, throwing is the only way out, so
  the class keeps earning its place.
- Half-initialization is then not an argument against the hook but a nudge on how to shape the
  route: the constructor builds the shell, the hook loads and checks. Vaadin's own idiom.

## Open questions

- `Q_wrapped_cause_test`: pin the wrapped-`IllegalArgumentException` path with a test, so a future
  Vaadin that drops `searchByCause` breaks loudly rather than silently 404ing? The probe is easy
  to revive.
- `Q_readme_mentions_it`: should README document the class at all? It currently doesn't, so the
  extension point is invisible unless you read the source.
- `Q_check_helper`: worth a one-line helper — `SimpleNavigationAccessControl.checkRole(..)` or
  similar — so the app doesn't hand-roll
  `if (!principal.hasRole(..)) throw new AccessRejectedException(..)`? Probably not; it saves one
  line and adds API.
- `Q_upstream_doc_bug`: **filed as flow#25748.** Nothing in `@AccessDeniedErrorRouter`'s javadoc says
  the exception class needs a public no-arg constructor; the requirement is stated two hops away, on
  `BeforeEvent.rerouteToError(Class)`, which is where `ReflectTools.createInstance` runs. The
  annotation's own javadoc example satisfies it by accident — an empty class body — so adding a
  message constructor to the exception silently breaks it. Probed: the denied user gets a 500
  (`InternalServerError`, `IllegalArgumentException` from `ReflectTools`) instead of the custom
  access-denied view. Asked for the sentence plus an example that carries an explicit no-arg
  constructor; suggested startup-time validation as a separate option.
- `Q_upstream_inert_reroute`: **filed as flow#25739, accepted as a defect.** `LocationChangeEvent`'s
  `rerouteTo` / `setStatusCode` are public, undeprecated, reachable from
  `AfterNavigationEvent.getLocationChangeEvent()`, and inert since Flow 1.0. Probed before filing:
  the reroute is dropped, the view is shown, `Router.navigate(..)` returns 200, nothing is logged.
  Note `rerouteTo` takes a `NavigationHandler` / `NavigationState`, not a `Class` — the trap costs a
  `NavigationStateBuilder` to fall into.
  Agreed upstream fix: deprecate the three reroute methods `forRemoval`; WARN from `rerouteTo`
  always and from `setStatusCode` only once the navigation is committed (the flag flips when
  `AfterNavigationEvent` is constructed, which is the only construction site and is after the status
  read), leaving the legitimate `HasErrorParameter` path — `ErrorStateRenderer.notifyNavigationTarget`
  — quiet. We argued against fail-fast: a throw there is caught by `Router.navigate`'s
  `catch (Exception)` and rendered as an error page, which is less legible than the no-op. Open:
  whether the deprecation is backported to 24.x, where most of the exposed users are.
