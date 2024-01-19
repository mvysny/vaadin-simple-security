package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.router.AccessDeniedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * An exception thrown when the current user does not have access to given route.
 * Note that vaadin-simple-security will not throw this exception on itself:
 * you should throw this exception when you implement your custom authorization logic in
 * the route's `AfterNavigationHandler.afterNavigation()` (or in the route constructor).
 * <p></p>
 * For example, often the route takes an ID of a document as a parameter, and you need to check whether
 * the current user can access that particular document. This case can not be handled by the simple [AllowRoles] logic.
 * <p></p>
 * You are responsible for catching of this exception in Vaadin's `HasErrorParameter`.
 */
public class AccessRejectedException extends AccessDeniedException {
    @NotNull
    private final String message;
    /**
     * The view which was navigated to. null if the exception was not thrown upon navigation but rather on e.g. button click.
     */
    @Nullable
    private final Class<?> routeClass;
    /**
     * Which roles were missing. May be empty if the exception is thrown because the [AllowRoles] annotation is missing on the view, or there
     * is some other reason for which the set of missing roles can not be provided.
     */
    @NotNull
    private final Set<String> missingRoles;

    /**
     * Creates the exception.
     * @param message {@link Throwable#getMessage()}.
     * @param routeClass the view which was navigated to. null if the exception was not thrown upon navigation but rather on e.g. button click.
     * @param missingRoles Which roles were missing. May be empty if the exception is thrown because the [AllowRoles] annotation is missing on the view, or there
     * is some other reason for which the set of missing roles can not be provided.
     */
    public AccessRejectedException(@NotNull String message, @Nullable Class<?> routeClass, @NotNull Set<String> missingRoles) {
        super();
        this.message = Objects.requireNonNull(message);
        this.routeClass = routeClass;
        this.missingRoles = Objects.requireNonNull(missingRoles);
    }

    /**
     * The route which was navigated to. null if the exception was not thrown upon navigation but rather on e.g. button click.
     * @return the route to which the access is denied to.
     */
    @Nullable
    public Class<?> getRouteClass() {
        return routeClass;
    }

    /**
     * Which roles were missing. May be empty if the exception is thrown because the [AllowRoles] annotation is missing on the view, or there
     * is some other reason for which the set of missing roles can not be provided.
     * @return missing roles, may be empty.
     */
    @NotNull
    public Set<String> getMissingRoles() {
        return missingRoles;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
