# Decisions

Why this project is the way it is and not otherwise — FAQ-shaped: each entry is a question and
its current answer. Rewrite the answer when it changes; delete the entry when nobody asks any
more. An entry is earned by what it would cost to reverse — half the code base — or by research
the next person would otherwise redo (cited as its `R_`). Not an entry: windows → panels
"because that's the trend", this red over that red, `get_foo` over `is_foo?`, the testing library,
the CI host, a version bump — a comment at the site of the choice, or nothing; nothing about
`design/` itself. Cite by slug, `D_<slug>`, never by position; `grep '^## D_' design/decisions.md`
is the index. The first entry is the ruler: every later one trims to its length — which is how
long this file gets, so keep it short. When you have written an entry, re-read it against the one
above, check it says nothing the doc comments already say, and cut what is left over.

---

## D_no_security_framework — Why extend Vaadin's `NavigationAccessControl` rather than use Spring Security, Shiro or servlet-container security?

Vaadin already ships the authorization half: `NavigationAccessControl` enforces the access
annotations and reroutes to a login view, asking the `HttpServletRequest` only for the principal
and its roles. We override those two lookups to read a session-scoped login service, so the
library is a session holder, a route guard and a password hash, and the app authenticates however
it likes. Why not Spring Security: a Vaadin Boot app has no Spring context to host it (**No Spring,
no Shiro**). Why not Shiro: it abstracts LDAP, Kerberos, SAML, OAuth2 and x509 behind an API far
larger than the login form it guards. Why not servlet-container security: users and roles then
live in the container's realm configuration, outside the app's database (**The application owns
its users**). Why no authentication API of our own: it would be either incomplete or as abstract
as Shiro. The cost we carry: every scheme beyond username + password is the app's own `login()`,
with `DirectLoginService` and `externalauth/google` as the only help.

## D_app_throws_its_own — Why ship no exception for an app's own access checks?

`AccessRejectedException` shipped through 1.x and is gone in 2.0. Extending Vaadin's
`AccessDeniedException` made it a routing exception wherever it was thrown: from a navigation hook
Flow's `searchBySuperType` hands it to `RouteAccessDeniedError`, which rewrites it to a bare 404 and
drops the message and roles it carried; from a click listener `ErrorHandlerUtil` matches on the
exact type, so the subclass is passed over where its parent would be intercepted. Strip that
superclass and what is left is a message and two fields an app declares in three lines — carrying
what its own error dialog needs rather than what we guessed. So the app throws its own exception and
shows it in its own `ErrorHandler`, and Vaadin's `AccessDeniedException` stays Vaadin's to throw.
The cost we carry: the rule is README prose, not a type the compiler enforces.
