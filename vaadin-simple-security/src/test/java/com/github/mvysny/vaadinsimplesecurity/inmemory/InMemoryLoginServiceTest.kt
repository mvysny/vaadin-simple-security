package com.github.mvysny.vaadinsimplesecurity.inmemory

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import javax.security.auth.login.FailedLoginException
import kotlin.test.expect

class InMemoryLoginServiceTest : DynaTest({
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
    beforeEach { MockVaadin.setup(routes) }
    afterEach { MockVaadin.tearDown() }
    group("isLoggedIn") {
        test("false by default") {
            expect(false) { InMemoryLoginService.get().isLoggedIn }
        }
        test("sets to true after successful login") {
            InMemoryLoginService.get().login("admin", "admin")
            expect(true) { InMemoryLoginService.get().isLoggedIn }
        }
        test("stays false after unsuccessful login") {
            expectThrows<FailedLoginException> {
                InMemoryLoginService.get().login("non-existing", "admin")
            }
            expect(false) { InMemoryLoginService.get().isLoggedIn }
        }
    }
    group("login") {
        test("rejects incorrect username") {
            expectThrows<FailedLoginException>("Invalid username or password") {
                InMemoryLoginService.get().login("non-existing", "admin")
            }
            expect(null) { InMemoryLoginService.get().currentUser }
            expect(setOf()) { InMemoryLoginService.get().currentUserRoles }
            expect(null) { InMemoryLoginService.get().currentPrincipal }
        }
        test("rejects incorrect password") {
            expectThrows<FailedLoginException>("Invalid username or password") {
                InMemoryLoginService.get().login("admin", "admin22")
            }
            expect(null) { InMemoryLoginService.get().currentUser }
            expect(setOf()) { InMemoryLoginService.get().currentUserRoles }
            expect(null) { InMemoryLoginService.get().currentPrincipal }
        }
        test("succeeds") {
            InMemoryLoginService.get().login("admin", "admin")
            expect("admin") { InMemoryLoginService.get().currentUser?.username }
            expect(setOf("admin")) { InMemoryLoginService.get().currentUserRoles }
            expect("admin") { InMemoryLoginService.get().currentPrincipal?.username }
        }
    }
})
