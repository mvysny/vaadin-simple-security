package com.github.mvysny.vaadinsimplesecurity

import com.vaadin.flow.router.AccessDeniedException
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * The library never throws this exception - the app throws it from its own authorization
 * logic, so these tests take the place of the usage the library itself doesn't have.
 */
class AccessRejectedExceptionTest {
    @Test fun `retains the message`() {
        // Vaadin's AccessDeniedException offers no message constructor, hence the overrides.
        val ex = AccessRejectedException("No rights to document 25", AdminView::class.java, setOf("admin"))
        expect("No rights to document 25") { ex.message }
        expect("No rights to document 25") { ex.localizedMessage }
    }
    @Test fun `retains the route and the missing roles`() {
        val ex = AccessRejectedException("Access denied", AdminView::class.java, setOf("admin", "sales"))
        expect<Class<*>?>(AdminView::class.java) { ex.routeClass }
        expect(setOf("admin", "sales")) { ex.missingRoles }
    }
    @Test fun `route class is null when not thrown upon navigation`() {
        val ex = AccessRejectedException("Access denied", null, setOf())
        expect(null) { ex.routeClass }
        expect(setOf()) { ex.missingRoles }
    }
    @Test fun `is a Vaadin AccessDeniedException`() {
        // that's what makes Vaadin reroute to the access-denied error view.
        expect<Class<*>>(AccessDeniedException::class.java) { AccessRejectedException::class.java.superclass }
    }
}
