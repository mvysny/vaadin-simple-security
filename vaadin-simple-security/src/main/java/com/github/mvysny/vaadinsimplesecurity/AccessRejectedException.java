package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.router.AccessDeniedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * Thrown by <i>your app</i> to abort a route the current user must not see, in a case
 * {@code @RolesAllowed} can not express - typically a per-document check, from a spot that
 * holds no {@link com.vaadin.flow.router.BeforeEvent} to reroute with:
 *
 * <pre>{@code
 * @Route("document/:documentId")
 * @PermitAll
 * public class DocumentRoute extends VerticalLayout implements AfterNavigationObserver {
 *     @Override
 *     public void afterNavigation(AfterNavigationEvent event) {
 *         final Document doc = Document.getById(event.getRouteParameters().getLong("documentId").orElseThrow());
 *         if (!doc.isVisibleTo(InMemoryLoginService.get().getCurrentPrincipal())) {
 *             // AfterNavigationEvent offers no rerouteTo(): throwing is the only way to stop here
 *             throw new AccessRejectedException("Document is not yours", getClass(), Set.of());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>Throwing <i>is</i> the feature: it aborts wherever you are. Where a {@code BeforeEvent} is
 * at hand - {@code beforeEnter()}, {@code HasUrlParameter.setParameter()} - take Vaadin's own way
 * out instead, {@code event.forwardTo(..)} or {@code event.rerouteToError(..)} followed by
 * {@code return}. This exception is for everywhere else: {@code afterNavigation()}, a click
 * listener, a service below the UI. The library itself never throws it - it guards routes by
 * role from a UI-level listener ({@link SimpleNavigationAccessControl}), before the route is
 * even constructed, and only your app knows the rest.
 *
 * <p>Uncaught, it lands in Vaadin's {@link com.vaadin.flow.router.RouteAccessDeniedError},
 * which rewrites it into a plain 404 - a route you may not see must not look different from
 * one that doesn't exist. To say more than that, give the app a route implementing
 * {@code HasErrorParameter<AccessRejectedException>}: {@link #getRouteClass()} and
 * {@link #getMissingRoles()} are there to tell the user what was missing.
 *
 * <p>Why this exists next to Vaadin's own {@link AccessDeniedException}: Vaadin never throws
 * that one - it names the class in {@code rerouteToError(Class, String)} and creates it
 * reflectively, so it must keep its no-arg constructor and carries nothing; the reason rides
 * along separately, as {@code ErrorParameter.getCustomMessage()}. An exception you throw
 * yourself has no such channel, so the message, the route and the roles live here,
 * {@link #getMessage()} included, backed by a field of its own.
 *
 * <p>For the same reason this class can not be named in
 * {@code @AccessDeniedErrorRouter(rerouteToError = ...)}: Vaadin would fail to instantiate it.
 */
public class AccessRejectedException extends AccessDeniedException {
    @NotNull
    private final String message;
    @Nullable
    private final Class<?> routeClass;
    @NotNull
    private final Set<String> missingRoles;

    /**
     * @param message the detail message, e.g. "Document 25 is not yours".
     * @param routeClass the route the access was rejected to; null if not thrown upon navigation
     *                   but on e.g. a button click.
     * @param missingRoles the roles the user would have needed; may be empty when the rejection
     *                     wasn't about roles in the first place.
     */
    public AccessRejectedException(@NotNull String message, @Nullable Class<?> routeClass, @NotNull Set<String> missingRoles) {
        super();
        this.message = Objects.requireNonNull(message);
        this.routeClass = routeClass;
        this.missingRoles = Objects.requireNonNull(missingRoles);
    }

    /**
     * @return the route the access was rejected to, or null if this wasn't thrown upon
     * navigation but on e.g. a button click.
     */
    @Nullable
    public Class<?> getRouteClass() {
        return routeClass;
    }

    /**
     * @return the roles the user would have needed; may be empty when the rejection wasn't
     * about roles in the first place.
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
