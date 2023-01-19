package com.github.mvysny.vaadinsimplesecurity;

import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * LoginService is usually a session-scoped service which holds currently logged-in user.
 * The service is simply stored in VaadinSession, storing the current user to session as well. Call {@link #getCurrentUser()}
 * to obtain the current user.
 * <p></p>
 * This is a skeletal implementation of the login service providing helpful methods. Your service should:
 * <ul>
 *     <li>Extend this class</li>
 *     <li>Have a static get() function which retrieves the instance of this service from VaadinSession.
 *     See {@link #get(Class, Supplier)} on how to implement this.</li>
 *     <li>Have a private constructor, so that it's not accidentally instantiated.</li>
 * </ul>
 * You can of course implement your service from scratch if you need, or if this skeletal implementation
 * doesn't suit your needs. We still recommend to go with a session-scoped service though.
 * @param <U> the type of the User object, for example a database entity holding user information.
 */
public abstract class AbstractLoginService<U extends Serializable> implements Serializable {
    /**
     * The main "Welcome" route of the app. {@link #login(Serializable)} will navigate here. Defaults to "".
     */
    @NotNull
    private final String mainRoutePath;
    @Nullable
    private U currentUser = null;

    protected AbstractLoginService() {
        this("");
    }

    /**
     * @param mainRoutePath The main "Welcome" route of the app. {@link #login(Serializable)} will navigate here.
     */
    protected AbstractLoginService(@NotNull String mainRoutePath) {
        // disallow direct instantiation since the service should offer a get() function, looking up
        // the service from VaadinSession.
        this.mainRoutePath = mainRoutePath;
    }

    /**
     * Returns the currently logged-in user, or null if there's no user logged in.
     * @return logged-in user, may be null.
     */
    @Nullable
    public U getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns true if the user is logged in (the {@link #getCurrentUser()} is not null), false if not.
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Logs in given user.
     * @param user the user to log in.
     */
    protected void login(@NotNull U user) {
        this.currentUser = user;

        // creates a new session after login, to prevent session fixation attack
        VaadinService.reinitializeSession(VaadinRequest.getCurrent());

        // navigate the user away from the LoginView and to the landing page.
        // all logged-in users must be able to see the landing page, otherwise they will
        // be redirected back to the LoginView.
        UI.getCurrent().navigate(mainRoutePath);
    }

    /**
     * Logs out the user, clears the session and reloads the page. Since no user
     * is logged in, {@link SimpleViewAccessChecker} will redirect Vaadin to the login page.
     */
    public void logout() {
        VaadinSession.getCurrent().close();

        // The UI is recreated by the page reload, and since there is no user in the session (since it has been cleared),
        // the UI will show the LoginView.
        UI.getCurrent().getPage().reload();
    }

    @NotNull
    public Set<String> getCurrentUserRoles() {
        if (getCurrentUser() == null) {
            return Collections.emptySet();
        }
        return getRoles(getCurrentUser());
    }

    /**
     * Returns all roles which given user possesses.
     * @param user the user, not null.
     * @return roles of given user. May be empty if the user has no roles.
     */
    @NotNull
    protected abstract Set<String> getRoles(@NotNull U user);

    public boolean isUserInRole(@NotNull String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Returns the service instance from Vaadin Session, creating it if it doesn't exist yet.
     * You should define the <code>get()</code> static function which simply calls this one:
     * <pre>
     * class MyLoginService extends AbstractLoginService&lt;User&gt; {
     *     private MyLoginService() {}
     *     public static MyLoginService get() {
     *         return get(MyLoginService.class, MyLoginService::new);
     *     }
     * }
     * </pre>
     * See {@link InMemoryLoginService} for an example.
     * @param serviceClass the class of your login service.
     * @param constructor creates new instances of your login service.
     * @return the service.
     */
    @NotNull
    protected static <S extends AbstractLoginService<U>, U extends Serializable> S get(@NotNull Class<S> serviceClass, @NotNull Supplier<S> constructor) {
        final VaadinSession session = Objects.requireNonNull(VaadinSession.getCurrent(), "Not called from Vaadin UI thread");
        S service = session.getAttribute(serviceClass);
        if (service == null) {
            service = constructor.get();
            session.setAttribute(serviceClass, service);
        }
        return service;
    }
}
