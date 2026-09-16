# Vaadin Simple Security

A library providing simple security support for Vaadin. Doesn't use nor depend on Spring. Perfect for
your [Vaadin Boot](https://github.com/mvysny/vaadin-boot)-based projects.
Implements ideas behind the [Securing Plain Java Applications](https://vaadin.com/docs/latest/security/advanced-topics/securing-plain-java-app)
Vaadin documentation, but doesn't depend on the servlet container security configuration -
instead the users are managed directly by the application.

Supports:

* *Authentication* - Only allow known users to access the app.
* *Authorization* - Only allow users with appropriate rights to access particular parts of the app.

Provides a demo in-memory user registry. You should store your users into a database table;
this library will assist you with providing best practices for storing passwords (hashing+salting).

The library is in Maven Central. Pick the version matching your Vaadin:

| Version               | Supported Vaadin | Required JDK |
|-----------------------|------------------|--------------|
| 2.x (this branch, not released yet) | 25+ | 21+          |
| 1.1                   | 24.3+            | 17+          |
| [0.2](../../tree/0.x) | 23+              | 11+          |

Then add it as a dependency via Gradle:
```kotlin
dependencies {
    implementation("com.github.mvysny.vaadin-simple-security:vaadin-simple-security:1.1")
}
```

Please see the [Vaadin Simple Security Example Project](https://github.com/mvysny/vaadin-simple-security-example).

## Let's Start

We'll start with a very simple in-memory user management. Afterwards, we'll switch to a SQL-based user table.

First, you'll need a login page. We'll implement the login page as a Vaadin Route, which will simply contain
the Vaadin LoginForm component. Vaadin's LoginForm enables proper browser username/password autocompletion;
`PasswordField` and `TextField` do not since the input element is hidden in ShadowDOM.

```java
@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginRoute extends VerticalLayout implements ComponentEventListener<AbstractLogin.LoginEvent> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(LoginRoute.class);

    @NotNull
    private final LoginForm login = new LoginForm();

    public LoginRoute() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.addLoginListener(this);
        final LoginI18n loginI18n = LoginI18n.createDefault();
        loginI18n.setAdditionalInformation("Log in as user/user or admin/admin");
        login.setI18n(loginI18n);
        add(login);
    }

    @Override
    public void onComponentEvent(@NotNull AbstractLogin.LoginEvent loginEvent) {
        try {
            InMemoryLoginService.get().login(loginEvent.getUsername(), loginEvent.getPassword());
        } catch (LoginException ex) {
            log.warn("Login failed", ex);
            login.setError(true);
        }
    }
}
```

We need to redirect the browser to that login page if there's no user logged in.
We will observe the Vaadin navigation, and on every navigation attempt we'll check
whether there is user logged in. If not, we'll redirect to the login page.

In order to hook into Vaadin navigation, we'll need to create a custom `VaadinServiceInitListener`.
Follow the [Service Init Listener Tutorial](https://vaadin.com/docs/latest/advanced/service-init-listener) to create
the init listener and register it to Vaadin. Then, we'll hook into the Vaadin navigation
by adding a `BeforeEnterListener` to all Vaadin `UI`s:

```java
public class ApplicationServiceInitListener implements VaadinServiceInitListener {
    // Handles authorization - pulls in the currently-logged-in user from given service and checks
    // whether the user can access given route.
    //
    // InMemoryLoginService remembers the currently logged-in user in the Vaadin Session; it
    // retrieves the users from the InMemoryUserRegistry.
    private final SimpleNavigationAccessControl accessControl = SimpleNavigationAccessControl.usingService(InMemoryLoginService::get);
    
    public ApplicationServiceInitListener() {
        // Let's create the users.
        InMemoryUserRegistry.get().registerUser(new InMemoryUser("user", "user", Set.of("ROLE_USER")));
        InMemoryUserRegistry.get().registerUser(new InMemoryUser("admin", "admin", Set.of("ROLE_USER", "ROLE_ADMIN")));
        accessControl.setLoginView(LoginRoute.class);
    }
    @Override
    public void serviceInit(ServiceInitEvent event) {
        // accessControl observes all navigation: if there's no user logged in then we'll redirect to the LoginRoute;
        // if the user is logged in but lacks the role, Vaadin turns that into a 404 - the reason is
        // shown in dev mode and suppressed in production, so that a route you may not see looks
        // exactly like a route that doesn't exist.
        event.getSource().addUIInitListener(e -> e.getUI().addBeforeEnterListener(accessControl));
    }
}
```
`SimpleNavigationAccessControl` is the main class which enforces security in an application.
It is able to redirect to a login page if there's no user logged in; it also checks whether
the current user has access to the route being navigated to.

How will `SimpleNavigationAccessControl` know which user is currently logged in? That's easy -
it will retrieve the user from the `InMemoryLoginService` service. `InMemoryLoginService` remembers
the currently logged-in user in the Vaadin session; when a user attempts to log in,
that user is compared against users stored in the `InMemoryUserRegistry`.

Most often the logged-in user will be stored in the session. We could store the user
to the session directly, then we could make the access control retrieve the user from the session
as follows:
```java
new SimpleNavigationAccessControl(() -> VaadinSession.getCurrent().getAttribute(SimpleUserWithRoles.class));
```
However, we are going to need functions that deal with the logins and logouts as well, and
it's good to keep everything neatly in a single class responsible for the current user management.
Therefore, it's better to have a dedicated session-scoped `LoginService` service class which not only stores the current user,
but also provide helpful functions such as `login(username, password)` and `logout()`.
That's exactly what the `InMemoryLoginService` provides.

`InMemoryLoginService` inherits lots of useful functions from `AbstractLoginService`:

* `getCurrentUser()` - the logged-in user (your own user object), or null if nobody is logged in.
* `getCurrentPrincipal()` - the same user reduced to a username + roles; that's all the access control ever sees.
* `isLoggedIn()`, `isUserInRole(role)`, `getCurrentUserRoles()` - convenience checks.
* `login(user)` - protected; called by your own `login()` once the user has been authenticated. Stores
  the user into the session, changes the HTTP session ID (defusing [session fixation](https://owasp.org/www-community/attacks/Session_fixation))
  and navigates to the app's main route.
* `logout()` - closes the Vaadin session, invalidates the HTTP session and reloads the page; with no user
  in the new session, the access control sends the browser to the login route.

The "main route" `login()` navigates to is `""` by default; pass another path to the
`AbstractLoginService(String)` constructor. Every logged-in user must be allowed to see that route -
if they aren't, they land back on the login page right after logging in.

And just like that, we now have the full authentication chain implemented!

To recap:

* `InMemoryUserRegistry` holds a list of users (usernames, passwords and roles) in memory. Later on
  we'll store the users in a SQL database, but in-memory will work for now.
* `InMemoryLoginService` holds the currently logged-in user in Vaadin session, and provides `login()`/`logout()` functions.
  Uses `InMemoryUserRegistry` when logging in.
* `SimpleNavigationAccessControl` checks access to Vaadin routes (performs authorization). Retrieves the currently-logged-in
   user from the `InMemoryLoginService`.

## Authorization

We use the [Role-Based Access Control (RBAC)](https://en.wikipedia.org/wiki/Role-based_access_control).
Every user in the app is assigned
a set of roles - a set of duties it is expected to perform in the app. Every Vaadin route
then declares roles allowed to see that particular view; only users which are assigned at least one
of the roles declared on the view are then allowed to visit that view.

For example, the following route may only be accessed by users having the `ROLE_ADMIN` role.
The role names are arbitrary strings of your choosing - there is no `ROLE_` prefix convention
built into the library.

```java
@Route("admin")
@RolesAllowed("ROLE_ADMIN")
public class AdminPage extends VerticalLayout {}
```

We previously registered `SimpleNavigationAccessControl` to observe all routes as they are navigated to.
`SimpleNavigationAccessControl` will use `AnnotatedViewAccessChecker` to read the annotations present on the route
and will make a decision
whether to allow access or not.

Please see the [Access Annotations](https://vaadin.com/docs/latest/security/advanced-topics/securing-plain-java-app/#access-annotations)
Vaadin documentation on what kind of authorization annotations are available.

### Checks the annotations can not express

`@RolesAllowed` answers "may this user see this route at all". It can not answer "is this document
yours" - that needs the data, so the check moves into your code. Where it goes depends on when it
can run:

| The check | Put it in |
|---|---|
| needs nothing but roles | `@RolesAllowed` on the route - nothing is constructed |
| can run during navigation, from the URL, the session or a service | `beforeEnter()`, or `HasUrlParameter.setParameter()` for a URL parameter: `event.forwardTo(..)` or `event.rerouteToError(..)`, then `return` |
| happens later - a button click, a service call | throw your own exception, and show it in your `ErrorHandler` |

The route's constructor is deliberately missing from that table. Navigating twice in a row to the
same route reuses the instance, so a check in the constructor runs once per instance, not once per
navigation - change what the check reads, navigate again, and it is skipped.

For the third row, declare an exception carrying whatever your error dialog needs to show:

```java
public class DocumentAccessRejectedException extends RuntimeException {
    private final long documentId;
    public DocumentAccessRejectedException(long documentId) {
        super("Document " + documentId + " is not yours");
        this.documentId = documentId;
    }
    public long getDocumentId() { return documentId; }
}
```

Throwing aborts wherever you are, which is the point: in a click listener there is no
`BeforeEvent` to reroute with, and a bare `return` only leaves your own method. Register an
`ErrorHandler` to turn it into feedback:

```java
public class ApplicationServiceInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(@NotNull ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> e.getSession().setErrorHandler(errorEvent -> {
            if (errorEvent.getThrowable() instanceof DocumentAccessRejectedException ex) {
                Notification.show(ex.getMessage());
            } else {
                new DefaultErrorHandler().error(errorEvent);
            }
        }));
    }
}
```

Do not throw Vaadin's own `com.vaadin.flow.router.AccessDeniedException` yourself: it is Vaadin's to
throw, and Vaadin already handles it. From a navigation hook it is rewritten into a 404 -
deliberately, since a route you may not see must not look different from one that does not exist, so
your message is dropped. From a click listener, an app that has not replaced the error handler gets
Vaadin's access-denied error view swapped into the page, with no navigation.

## Users stored in SQL

We recommend to use [jdbi-orm](https://gitlab.com/mvysny/jdbi-orm) or [JOOQ](https://www.jooq.org/) to access the database,
however you can of course use your favourite library.

An example of such a service follows:
```java
public final class MyLoginService extends AbstractLoginService<User> {
    private MyLoginService() {}
    
    public void login(@NotNull String username, @NotNull String password) throws LoginException {
        final User user = User.dao.findByUsername(username); // load the user from the database
        if (user == null) {
            throw new FailedLoginException("Invalid username or password");
        }
        if (!user.passwordMatches(password)) {
            throw new FailedLoginException("Invalid username or password");
        }
        login(user);
    }

    @Override
    protected @NotNull SimpleUserWithRoles toUserWithRoles(@NotNull User user) {
        return new SimpleUserWithRoles(user.getUsername(), user.getRoleSet());
    }

    @NotNull
    public static MyLoginService get() {
        return get(MyLoginService.class, MyLoginService::new);
    }
}
```

`MyLoginService` inherits the same set of functions from `AbstractLoginService` as `InMemoryLoginService` did above.

We can now instantiate `SimpleNavigationAccessControl` simply:
```java
SimpleNavigationAccessControl accessControl = SimpleNavigationAccessControl.usingService(MyLoginService::get);
```

The `User` entity represents a user stored in a database table. It implements `HasPassword` which
takes care of password hashing+salting, and of password verification.

```java
@Table("users")
public final class User implements Entity<Long>, HasPassword {
    private Long id;
    @NotNull
    private String username;
    @NotNull
    private String hashedPassword;
    @NotNull
    private String roles;
    
    // getters+setters omitted for brevity
    // equals, hashCode, toString() omitted for brevity

    public Set<String> getRoleSet() {
        final String r = getRoles();
        return r == null ? Set.of() : Set.of(r.split(","));
    }

    public static class UserDao extends Dao<User, Long> {
        public UserDao() {
            super(User.class);
        }

        @Nullable
        public User findByUsername(String username) {
            return findSingleBy("username = :username", q -> q.bind("username", username));
        }
    }

    public static final UserDao dao = new UserDao();
}
```

The SQL DDL for the `users` table is as follows:
```sql
create table users (
  id bigint auto_increment primary key not null,
  username varchar(100) not null,
  hashedPassword varchar(200) not null,
  roles varchar(400) not null
);
create unique index idx_users_username on users(username);
```

To create the users in the database, simply call

```java
final User admin = new User();
admin.setUsername("admin");
admin.setPassword("admin");
admin.setRoles("ROLE_ADMIN,ROLE_USER");
admin.save();
final User user = new User();
user.setUsername("user");
user.setPassword("user");
user.setRoles("ROLE_USER");
user.save();
```

Please see the [Vaadin Simple Security Example Application](https://github.com/mvysny/vaadin-simple-security-example)
for a full example.

## Using with External Authentication Systems

There are many existing authentication systems which take care of user authentication
for you. Examples of such systems include OAuth 2.0, OpenId, LDAP or [Google Identity](https://developers.google.com/identity/gsi/web/guides/overview).
Vaadin Simple Security supports a couple of basic scenarios and provides tips
for more complex authentication cases.

This is for example the workflow of the "Google Sign In Button":

* You place a div on your web page. Upon clicking, the div calls Google Sign In javascript code.
* The javascript code shows the login window and guides the user to log in with their Google account,
  possibly handling any 2FA and/or password resets and such.
* Ultimately, upon successful authentication, the Google JavaScript code calls your JavaScript function,
  passing in a security token. The security token contains the user's e-mail and a
  digital signature from Google, proving that the user exists in the external system.
  * The token is then passed server-side, where it **must** be validated - a client-side check can be
    spoofed by any rogue script on the page.
  * The best way to pass it is the Vaadin RPC mechanism (a `@ClientCallable` method), since it goes through
    the Vaadin Servlet and obtains the Vaadin session lock, allowing you to use standard Vaadin machinery
    on successful authentication.
  * If the token is valid, you store the user into the Vaadin session,
    concluding the authentication procedure. The user is now logged in, and you navigate to the main view.
* Alternatively, upon successful authentication, Google JavaScript code navigates to a URL of your choice,
  passing in a security token, e.g. as a query parameter. You create a Vaadin Route for this purpose;
  the route extracts the security token from the query parameter and proceeds with the token validation
  as described above.

Vaadin Simple Security offers a direct support for some external identity providers; please see the documentation
below for concrete authentication procedures:

* [Vaadin Simple Security module for Google Identity](externalauth/google/README.md); synonyms: Google SSO, Sign in with Google, One Tap with Google.

Security tip: a valid Google token only proves that some Google account exists - not that it's *your*
user. Always check that the e-mail belongs to your organization; a check that the address ends with
`@yourcompany.com` or such is quite enough. Throw `FailedLoginException` otherwise.

### DirectLoginService

This case applies when you only use some kind of external authentication system, and
you do not have the list of users stored locally, for example in a SQL database.
In such case you don't have to extend `AbstractLoginService` but instead use a pre-provided
service called `DirectLoginService`, for example:

```java
googleSignInButton.addSignInListener(event -> {
    if (event.isError()) {
        onLoginFailed(event.getFailure());
        return;
    }
    // the ID token has already been verified server-side, so this e-mail can be trusted
    final String email = event.getUserInfo().email();
    if (!email.endsWith("@yourcompany.com")) {
        onLoginFailed(new FailedLoginException("Only @yourcompany.com e-mails are accepted"));
        return;
    }
    DirectLoginService.get().login(email, Set.of("ROLE_USER"));
});
```
where `onLoginFailed()` is your own:
```java
private void onLoginFailed(@NotNull Throwable failure) {
    log.warn("Google Login failed", failure);
    Notification.show("Login failed: " + failure.getMessage())
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
}
```

### Using both external authentication system and a locally stored users

This scenario is used when you have the list of users stored locally (e.g. in a SQL database), but you
also want to support an external authentication system, for example "Sign in with Google".
Here we expect that you already have a login service implemented. The solution is easy: just add a Java
function to your existing login service which performs the direct login, similar to `DirectLoginService.login()`;
you then call the `loginDirectly()` function after you validate the security token.

An example of such a `loginDirectly()` function:
```java
public final class MyLoginService extends AbstractLoginService<User> {
  // login(username, password) and toUserWithRoles() as above

  /**
   * Logs in given user, no password asked. Expects that the user has already been
   * authenticated by an external authentication system, and that its security token
   * has been validated server-side.
   */
  public void loginDirectly(@NotNull String email) throws LoginException {
    final User user = User.dao.findByUsername(email);
    if (user == null) {
      throw new FailedLoginException("Invalid user");
    }
    // all looks good: store the user into the Vaadin session, concluding the
    // authentication process, and navigate to the main route.
    login(user);
  }
}
```

The example above only lets in users that already have a local account. To create the account on
first sign-in instead, replace the `throw` with the account creation - but keep a check that the
e-mail belongs to your organization, otherwise anyone with a Google account gets in:
```java
User user = User.dao.findByUsername(email);
if (user == null) {
  if (!email.endsWith("@yourcompany.com")) {
    throw new FailedLoginException("Invalid user");
  }
  user = new User();
  user.setUsername(email);
  user.setRoles("ROLE_USER");
  user.save();
}
login(user);
```

## Other Authentication Mechanisms

There are many security frameworks already present in Java. However, while attempting
to support all authentication/authorization schemes those frameworks have become highly
abstract and hard to understand. And rightly so: the authentication schemes are wildly
variant - username+password against a SQL database or against LDAP/AD, client-side x509
certificates, Kerberos tickets via NTLM/SPNEGO (the Waffle library), SAML, OAuth2, smart cards,
fingerprints, the servlet container's own `ServletContext.login()`, all of it with or without SSO.

It is impossible to create an API covering all those cases without going abstraction-crazy.
That's why we deliberately avoid to use an all-encompassing library like [Apache Shiro](https://shiro.apache.org/)
or [Spring Security](https://projects.spring.io/spring-security/)
with insanely complex APIs. We also don't provide our own authentication API (since it would
either be incomplete or complex). In this case, the best abstraction is no abstraction at all.

Your best bet is to implement your own `MyLoginService` and offer a `login()` function which
could for example authenticate the user against an LDAP server.

Alternatively, you can try to configure the servlet container security instead,
follow the [Securing Plain Java App](https://vaadin.com/docs/latest/security/advanced-topics/securing-plain-java-app) tutorial
and not use this library.
