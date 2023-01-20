package com.github.mvysny.vaadinsimplesecurity

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10._expectInternalServerError
import com.github.mvysny.kaributesting.v10.expectView
import com.github.mvysny.kaributesting.v10.mock.MockedUI
import com.github.mvysny.kaributools.navigateTo
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryLoginService
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUser
import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUserRegistry
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.server.VaadinRequest
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed
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

class VokViewAccessCheckerTest : DynaTest({
    lateinit var routes: Routes
    beforeGroup {
        routes = Routes().autoDiscoverViews("com.github.mvysny.vaadinsimplesecurity")
        InMemoryUserRegistry.get().clear()
        InMemoryUserRegistry.get().registerUser(InMemoryUser("admin", "admin", setOf("admin")))
        InMemoryUserRegistry.get().registerUser(InMemoryUser("user", "user", setOf("user")))
        InMemoryUserRegistry.get().registerUser(InMemoryUser("sales", "sales", setOf("sales")))
    }
    afterGroup {
        InMemoryUserRegistry.get().clear()
    }
    beforeEach {
        MockVaadin.setup(routes, uiFactory = { MockedUIWithViewAccessChecker() })
    }
    afterEach {
        MockVaadin.tearDown()
    }

    test("no user logged in") {
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
    test("admin logged in") {
        InMemoryLoginService.get().login("admin", "admin")
        navigateTo<AdminView>()
        expectView<AdminView>()

        // Vaadin 23.1.0 throws NotFoundException instead of redirecting to LoginView if user is logged in.
        expectThrows<NotFoundException>("No route found for 'user': Access denied") {
            navigateTo<UserView>()
        }

        expectThrows<NotFoundException>("No route found for 'sales/sale': Access denied") {
            navigateTo<SalesView>()
        }

        expectThrows<NotFoundException>("No route found for 'rejectall': Access denied") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    test("user logged in") {
        InMemoryLoginService.get().login("user", "user")

        // Vaadin 23.1.0 throws NotFoundException instead of redirecting to LoginView if user is logged in.
        expectThrows<NotFoundException>("No route found for 'admin': Access denied") {
            navigateTo<AdminView>()
        }

        navigateTo<UserView>()
        expectView<UserView>()

        navigateTo<SalesView>()
        expectView<SalesView>()

        // Vaadin 23.1.0 throws NotFoundException instead of redirecting to LoginView if user is logged in.
        expectThrows<NotFoundException>("No route found for 'rejectall': Access denied") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    test("sales logged in") {
        InMemoryLoginService.get().login("sales", "sales")

        // Vaadin 23.1.0 throws NotFoundException instead of redirecting to LoginView if user is logged in.
        expectThrows<NotFoundException>("No route found for 'admin': Access denied") {
            navigateTo<AdminView>()
        }

        expectThrows<NotFoundException>("No route found for 'user': Access denied") {
            navigateTo<UserView>()
        }

        navigateTo<SalesView>()
        expectView<SalesView>()

        expectThrows<NotFoundException>("No route found for 'rejectall': Access denied") {
            navigateTo<RejectAllView>()
        }

        // VokViewAccessChecker won't navigate away from LoginView - it's the app's
        // responsibility to navigate to some welcome view after successful login.
        navigateTo<LoginView>()
        expectView<LoginView>()
    }
    test("error route not hijacked by the LoginView") {
        UI.getCurrent().addBeforeEnterListener { e ->
            e.rerouteToError(RuntimeException("Simulated"), "Simulated")
        }
        navigateTo(WelcomeView::class)
        _expectInternalServerError("Simulated")
    }
})

class MockedUIWithViewAccessChecker : MockedUI() {
    override fun init(request: VaadinRequest) {
        super.init(request)
        val checker = SimpleViewAccessChecker.usingService { InMemoryLoginService.get() }
        checker.setLoginView(LoginView::class.java)
        addBeforeEnterListener(checker)
    }
}
