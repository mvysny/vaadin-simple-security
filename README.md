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

## Let's Start

First, you'll need a login page. 

TODO

## Authentication

TODO

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

## 