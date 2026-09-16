# Ideas

Brainstorm tracker — half-formed thoughts, open questions, things we measured but haven't decided
on. Not part of the doc layer (`AGENTS.md` + `design/`): nothing here is normative, and nothing
cites it. An idea that settles moves out — to a `D_` entry, an invariant, a doc comment, code —
and is deleted here.

---

## Should we still tell users to throw `AccessRejectedException` themselves?

Open. Started from "the class has zero usages"; ended up somewhere more interesting.

### What Vaadin actually gives you

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
| `afterNavigation` | `AfterNavigationEvent`: getters only | **none** | throwing only |
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

### The case-by-case tree

- **User may not see the route at all** → `@RolesAllowed` on the class. Cheapest and the only
  option that constructs nothing. No exception involved. Agreed.
- **Route opens one DB entry the user may not read, id in the URL** → `setParameter`, with
  `event.rerouteToError(..)` / `forwardTo(..)` + `return`. Vaadin-idiomatic. Note this does *not*
  "skip init": the constructor already ran. It only works cleanly if the constructor builds the
  empty shell and `setParameter` loads the data — which is the Vaadin idiom anyway.
- **The input isn't in the URL** — a header ComboBox writes a selection into the session, the
  route reads it eagerly. This is the interesting one, and the reason the class exists.

### The trap that decides it (probed, not theory)

Navigating to the same route three times, with a detour in between:

```
ctor = 2     beforeEnter = 3     afterNavigation = 3
```

Two consecutive navigations to the same route **reuse the instance** — the constructor does not
run again (`sendBeforeEnterEventToExistingChain`). So:

> **A security check in a constructor runs once per instance, not once per navigation.**

For the ComboBox case that is exactly wrong: change the selection, navigate to the same route
again, and the constructor check is skipped. `beforeEnter` and `afterNavigation` run every time.

### Where that leaves us

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

### Open questions

- Is the wrapped-`IllegalArgumentException` path worth a test, so a future Vaadin that drops
  `searchByCause` breaks loudly rather than silently 404ing? (The probe is easy to revive.)
- Should README document the class at all? It currently doesn't mention it, so the extension
  point is invisible unless you read the source.
- Worth a one-line helper — `SimpleNavigationAccessControl.checkRole(..)` or similar — so the
  app doesn't hand-roll `if (!principal.hasRole(..)) throw new AccessRejectedException(..)`?
  Probably not; it saves one line and adds API.
- Upstream: nothing in `@AccessDeniedErrorRouter`'s javadoc says the exception class needs a
  public no-arg constructor (Flow instantiates it reflectively). A doc bug worth reporting.
