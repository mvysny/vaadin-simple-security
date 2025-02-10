# External Authentication via Google Identity service

Provides Vaadin component which implements the secure authentication via Google Identity Service -
the "Sign in with Google" button. It:

* allows the user to log in securely at Google
* verifies the login information securely at server-side with Google, so that the login token can not be spoofed by a rogue JavaScript code
* Fires a Vaadin event once everything is okay

## Preparation

It's good to read the documentation on Google Identity, namely the following tutorials:

* Obtain the Google ID token via [the Sign In With Google button](https://developers.google.com/identity/gsi/web/guides/display-button) -
  we'll use the JavaScript button rendering.
* [Verify the Google ID token server-side](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token)

Follow the [Setup Tutorial](https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid) and create
a `OAuth 2.0 Client ID` at console.cloud.google.com. You'll obtain the Client ID
number (looks like `2398471023-asoifywerhewjkdlaj023842asdkl.apps.googleusercontent.com`) which
is necessary in order to be used with the `GoogleSignInButton`.

In order to see how things work, see the [Google OAuth with Vaadin](https://mvysny.github.io/vaadin-google-oauth/) blogpost.

## Code Example / Using with your project

The library is in Maven Central. To use from your app, add this library as a dependency via Gradle:
```kotlin
dependencies {
    implementation("com.github.mvysny.vaadin-simple-security:externalauth-google:1.1")
}
```

To create the button:
```java
public class LoginRoute extends VerticalLayout {
  private var googleSignInButton = new GoogleSignInButton(CLIENT_ID);
  public LoginRoute() {
    // ...
    add(googleSignInButton);
    googleSignInButton.addSignInListener(e -> {
      if (e.isOK()) {
        try {
          var userInfo = e.getUserInfo();
          if (!userInfo.getEmail().endsWith("@yourcompany.com")) {
            // additional check that the e-mail indeed belongs to the company.
            throw new FailedLoginException("Invalid e-mail address");
          }
          MyLoginService.get().loginDirectly(userInfo.getEmail());
        } catch (Exception ex) {
            onLoginFail(ex);
        }
      } else {
          onLoginFail(e.getFailure());
      }
    });
  }
  private void onLoginFail(Exception ex) {
      log.error("Login failed", ex);
      Notification.show("Login failed: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
  }
}
```

Then, add the `GoogleSignInButton` somewhere to your login UI:

* If using Vaadin `LoginOverlay`, add it to the custom form area: `loginOverlay.getCustomFormArea().add(googleSignInButton)`
* If using Vaadin `LoginForm`, it unfortunately [doesn't support the custom form area](https://github.com/vaadin/flow-components/issues/5582). Yet, the form is probably nested in a `VerticalLayout`;
  just add the Google Sign In Button to the VerticalLayout, right under the `LoginForm` and you're good.

## Further reading

Please make sure to read the "Using with External Authentication Systems" Vaadin Simple Security
documentation, to correctly understand how to use this kind of authentication.
