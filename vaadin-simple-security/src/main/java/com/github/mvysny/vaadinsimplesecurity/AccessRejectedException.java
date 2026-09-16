package com.github.mvysny.vaadinsimplesecurity;

import com.vaadin.flow.router.AccessDeniedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * Thrown by <i>your app</i> when the current user must not see a route, in a case
 * {@code @RolesAllowed} can not express - typically a per-document check:
 *
 * <pre>{@code
 * @Route("document")
 * @PermitAll
 * public class DocumentRoute extends VerticalLayout implements HasUrlParameter<Long> {
 *     public void setParameter(BeforeEvent event, Long documentId) {
 *         final Document doc = Document.getById(documentId);
 *         if (!doc.isVisibleTo(InMemoryLoginService.get().getCurrentPrincipal())) {
 *             throw new AccessRejectedException("Document " + documentId + " is not yours", getClass(), Set.of());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>vaadin-simple-security never throws this itself - {@link SimpleNavigationAccessControl}
 * guards routes by role, and only your app knows the rest.
 *
 * <p>Uncaught, it lands in Vaadin's {@link com.vaadin.flow.router.RouteAccessDeniedError},
 * the generic 403 page. To show your own, give the app a route implementing
 * {@code HasErrorParameter<AccessRejectedException>}: {@link #getRouteClass()} and
 * {@link #getMissingRoles()} are there to tell the user what was missing.
 *
 * <p>Why this exists at all, next to Vaadin's own {@link AccessDeniedException}: that one is
 * parameterless - no message, no route, no roles - so everything a 403 page would show has
 * to be added here, {@link #getMessage()} included, backed by a field of its own.
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
