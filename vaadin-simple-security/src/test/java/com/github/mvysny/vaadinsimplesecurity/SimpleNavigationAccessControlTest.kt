package com.github.mvysny.vaadinsimplesecurity

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributesting.v10.mock.MockedUI
import com.github.mvysny.kaributools.navigateTo
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUser
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUserRegistry
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.AccessDeniedException
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.server.VaadinRequest
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * A view with no parent layout.
 */
@RolesAllowed("admin")
@Route("admin")
class AdminView : VerticalLayout()

@Route("login")
class LoginView : VerticalLayout()

class MyLayout : VerticalLayout(), RouterLayout

@Route("", layout = MyLayout::class)
@PermitAll
class WelcomeView : VerticalLayout()

/**
 * A view with parent layout.
 */
@Route("user", layout = MyLayout::class)
@RolesAllowed("user")
class UserView : VerticalLayout()

@RolesAllowed("sales")
class SalesLayout : VerticalLayout(), RouterLayout

/**
 * This view can not be effectively viewed with 'user' since its parent layout lacks the 'user' role.
 */
@RolesAllowed("sales", "user")
@Route("sales/sale", layout = SalesLayout::class)
class SalesView : VerticalLayout()

/**
 * This view can not be effectively viewed with anybody.
 */
@RolesAllowed()
@Route("rejectall")
class RejectAllView : VerticalLayout()

class SimpleNavigationAccessControlTest {
    companion object {
        private lateinit var routes: Routes
        @BeforeAll @JvmStatic fun setup() {
            routes = Routes().autoDiscoverViews("com.github.mvysny.vaadinsimplesecurity")
            InMemoryUserRegistry.get().clear()
            InMemoryUserRegistry.get().registerUser(InMemoryUser("admin", "admin", setOf("admin")))
            InMemoryUserRegistry.get().registerUser(InMemoryUser("user", "user", setOf("user")))
            InMemoryUserRegistry.get().registerUser(InMemoryUser("sales", "sales", setOf("sales")))
        }
        @AfterAll @JvmStatic fun teardown() {
            InMemoryUserRegistry.get().clear()
        }
    }
    @BeforeEach fun setupVaadin() {
        MockVaadin.setup(routes, uiFactory = { MockedUIWithViewAccessChecker() })
    }
    @AfterEach fun teardownVaadin() {
        MockVaadin.tearDown()
    }

    @Test fun `no user logged in`() {
        expect(false) { InMemoryLoginService.get().isLoggedIn }
        navigateTo<AdminView>()
        expectView<LoginView>()

        navigateTo<UserView>()
        expectView<LoginView>()

        navigateTo<SalesView>()
        expectView<LoginView>()

        navigateTo<RejectAllView>()
        expectView<LoginView>()

        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    @Test fun `admin logged in`() {
        InMemoryLoginService.get().login("admin", "admin")
        navigateTo<AdminView>()
        expectView<AdminView>()

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<UserView>()
        }

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<SalesView>()
        }

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    @Test fun `user logged in`() {
        InMemoryLoginService.get().login("user", "user")

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<AdminView>()
        }

        navigateTo<UserView>()
        expectView<UserView>()

        navigateTo<SalesView>()
        expectView<SalesView>()

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    @Test fun `sales logged in`() {
        InMemoryLoginService.get().login("sales", "sales")

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<AdminView>()
        }

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<UserView>()
        }

        navigateTo<SalesView>()
        expectView<SalesView>()

        expectThrows<AccessDeniedException>("Access is denied by annotations on the view.") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    @Test fun `error route not hijacked by the LoginView`() {
        UI.getCurrent().addBeforeEnterListener { e ->
            e.rerouteToError(RuntimeException("Simulated"), "Simulated")
        }
        navigateTo(WelcomeView::class)
        _expectInternalServerError("Simulated")
    }
}

class MockedUIWithViewAccessChecker : MockedUI() {
    override fun init(request: VaadinRequest) {
        super.init(request)
        val accessControl = SimpleNavigationAccessControl.usingService { InMemoryLoginService.get() }
        accessControl.setLoginView(LoginView::class.java)
        addBeforeEnterListener(accessControl)
    }
}
