# Vaadin Simple Security

A library providing simple security support for Vaadin. Doesn't use nor depend on Spring. Perfect for
your [Vaadin Boot](https://github.com/mvysny/vaadin-boot)-based projects.

Supports:

* *Authentication* - Only allow known users to access the app.
* *Authorization* - Only allow users with appropriate rights to access particular parts of the app.

Provides a demo in-memory user registry. You should store your users into a database table;
this library will assist you with providing best practices for storing passwords (hashing+salting).

The library is in Maven Central. To use from your app, add this library as a dependency via Gradle:
```kotlin
dependencies {
    implementation("com.github.mvysny.vaadin-simple-security:vaadin-simple-security:0.1")
}
```

TODO example project.

## Let's Start

We'll start with a very simple in-memory user management. Afterwards, we'll switch to a SQL-based user table.

First, you'll need a login page. We'll implement a Vaadin Route which will simply contain
Vaadin's LoginForm. Vaadin's LoginForm enables proper browser username/password autocompletion (`PasswordField` and `TextField` do not since the input element is hidden in ShadowDOM),
that's why it's important to use LoginForm.

TODO LoginRoute example.

We need to redirect the browser to that login page if there's no user logged in.
We will observe the Vaadin navigation, and on every navigation attempt we'll check
whether there is user logged in. If not, we'll redirect to the login page.

In order to hook into Vaadin navigation, we'll need to create a custom `VaadinServiceInitListener`.
Follow the [Service Init Listener Tutorial](https://vaadin.com/docs/v14/flow/advanced/tutorial-service-init-listener) to create
the init listener and register it to Vaadin. Then, we'll hook into the Vaadin navigation
by adding a `BeforeEnterListener` to all Vaadin `UI`s:

```java
import java.util.Set;

public class ApplicationServiceInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        // let's create the "administrator" user, with "password" as password, having the "admin" role.
        InMemoryUserRegistry.get().registerUser(new InMemoryUser("administrator", "password", Set.of("admin")));
        event.getSource().addUIInitListener(e -> {
            SimpleViewAccessChecker accessChecker = new SimpleViewAccessChecker(new InMemoryLoggedInUserProvider());
            accessChecker.setLoginView(LoginRoute.class);
            e.getUI().addBeforeEnterListener(accessChecker);
        });
    }
}
```

How will `SimpleViewAccessChecker` know which user is currently logged in? That's easy -
we will have to implement the `LoggedInUserProvider` which will return the currently logged-in user.

Most often the logged-in user will be stored in the session. We can store the user
to the session directly, however where will we put functions dealing with logins and logouts?
Therefore, it's better to have a dedicated session-scoped `LoginService`, which will not only store the current user,
but also provide helpful functions such as `login(username, password)` and `logout()`.
That's exactly what the `InMemoryLoginService` provides.

`InMemoryLoginService` inherits lots of useful functions from `AbstractLoginService`, most importantly:

* `getCurrentUser()` - returns the currently logged-in user.
* `logout()` - performs logout and redirects to the LoginRoute

And just like that, we now have the full authentication chain implemented!

## Authorization

We use the [Role-Based Access Control (RBAC)](https://en.wikipedia.org/wiki/Role-based_access_control).
Every user in the app is assigned
a set of roles - a set of duties it is expected to perform in the app. Every Vaadin route
then declares roles allowed to see that particular view; only users which are assigned at least one
of the roles declared on the view are then allowed to visit that view.

For example, the following route may only be accessed by users that contain the `admin` role.

```java
@Route("admin")
@RolesAllowed("admin")
public class AdminPage extends VerticalLayout {}
```

We previously registered `SimpleViewAccessChecker` to observe all routes as they are navigated to.
`SimpleViewAccessChecker` will read the annotations present on the route and will make a decision
whether to allow access or not.

## Users stored in SQL

We recommend to use [jdbi-orm](https://gitlab.com/mvysny/jdbi-orm) to access the database,
however you can of course use your favourite library.

An example of such a service follows:
```java
public final class MyLoginService extends AbstractLoginService<User> {
    private MyLoginService() {}
    
    public void login(@NotNull String username, @NotNull String password) throws LoginException {
        final User user = User.findByUsername(username); // load the user from the database
        if (user == null) {
            throw new FailedLoginException("Invalid username or password");
        }
        if (!user.passwordMatches(password)) {
            throw new FailedLoginException("Invalid username or password");
        }
        login(user);
    }

    @NotNull
    public static MyLoginService get() {
        return get(MyLoginService.class, MyLoginService::new);
    }
}
```

`MyLoginService` inherits lots of useful functions from `AbstractLoginService`, most importantly:

* `getCurrentUser()` - returns the currently logged-in user.
* `logout()` - performs logout and redirects to the LoginRoute

We can now implement the `MyLoggedInUserProvider` easily:
```java
public class MyLoggedInUserProvider implements LoggedInUserProvider {
    @Override
    public @Nullable Principal getCurrentUser() {
        final User currentUser = MyLoginService.get().getCurrentUser();
        return currentUser == null ? null : new BasicUserPrincipal(currentUser.getUsername());
    }

    @Override
    public @NotNull Set<String> getCurrentUserRoles() {
        final User currentUser = MyLoginService.get().getCurrentUser();
        return currentUser == null ? Collections.emptySet() : currentUser.getRoles();
    }
}
```

The `User` entity represents an user stored in a database table. It implements `HasPassword` which
takes care of password hashing+salting, and of password verification.

TODO User entity example, plus SQL DDL of the Users table.

## Other Authentication mechanisms

There are many security frameworks already present in Java. However, while attempting
to support all authentication/authorization schemes those frameworks have became highly
abstract and hard to understand. And rightly so: the authentication schemes are wildly
variant:

* Authentication using username + password:
    * Against a local SQL database of users
    * Against a LDAP/AD server
* Client-side x509 certificates
* Kerberos-provided security token (client-to-server tickets):
    * Authentication via NTLM/SPNEGO/Windows login via a NTLM servlet filter (the Waffle library)
* SAML-based solutions which are anything but simple
* Oauth2
* Other means: smart cards, fingerprints, ...
* All that while supporting SSO, or using the Servlet container-provided authentication mechanism
  (the `ServletContext.login()` method).

It is impossible to create an API convering all those cases without going abstraction-crazy.
That's why we deliberately avoid to use an all-encompassing library like [Apache Shiro](https://shiro.apache.org/)
or [Spring Security](https://projects.spring.io/spring-security/)
with insanely complex APIs. We also don't provide our own authentication API (since it would
either be incomplete or complex). In this case, the best abstraction is no abstraction at all.

However, if need be, we may add support for most used combinations (e.g. username+password via LDAP).
A standalone library will then be created.

Your best bet is to implement your own `MyLoginService` and offer a `login()` function which
could for example authenticate the user against an LDAP server.

TODO steal more from vok security.md