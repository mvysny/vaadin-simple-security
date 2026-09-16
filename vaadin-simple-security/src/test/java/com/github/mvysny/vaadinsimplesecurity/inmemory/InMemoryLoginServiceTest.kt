package com.github.mvysny.vaadinsimplesecurity.inmemory

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.vaadinsimplesecurity.expectThrows
import org.junit.jupiter.api.*
import javax.security.auth.login.FailedLoginException
import kotlin.test.expect

class InMemoryLoginServiceTest {
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
    @BeforeEach fun setupVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun teardownVaadin() { MockVaadin.tearDown() }
    @Nested inner class isLoggedIn() {
        @Test fun `false by default`() {
            expect(false) { InMemoryLoginService.get().isLoggedIn }
        }
        @Test fun `sets to true after successful login`() {
            InMemoryLoginService.get().login("admin", "admin")
            expect(true) { InMemoryLoginService.get().isLoggedIn }
        }
        @Test fun `stays false after unsuccessful login`() {
            assertThrows<FailedLoginException> {
                InMemoryLoginService.get().login("non-existing", "admin")
            }
            expect(false) { InMemoryLoginService.get().isLoggedIn }
        }
    }
    @Nested inner class login {
        @Test fun `rejects incorrect username`() {
            expectThrows<FailedLoginException>("Invalid username or password") {
                InMemoryLoginService.get().login("non-existing", "admin")
            }
            expect(null) { InMemoryLoginService.get().currentUser }
            expect(setOf()) { InMemoryLoginService.get().currentUserRoles }
            expect(null) { InMemoryLoginService.get().currentPrincipal }
        }
        @Test fun `rejects incorrect password`() {
            expectThrows<FailedLoginException>("Invalid username or password") {
                InMemoryLoginService.get().login("admin", "admin22")
            }
            expect(null) { InMemoryLoginService.get().currentUser }
            expect(setOf()) { InMemoryLoginService.get().currentUserRoles }
            expect(null) { InMemoryLoginService.get().currentPrincipal }
        }
        @Test fun succeeds() {
            InMemoryLoginService.get().login("admin", "admin")
            expect("admin") { InMemoryLoginService.get().currentUser?.username }
            expect(setOf("admin")) { InMemoryLoginService.get().currentUserRoles }
            expect("admin") { InMemoryLoginService.get().currentPrincipal?.username }
        }
    }
    @Nested inner class loginDirectly {
        @Test fun `logs in without a password`() {
            InMemoryLoginService.get().loginDirectly(InMemoryUser("admin", "admin", setOf("admin")))
            expect("admin") { InMemoryLoginService.get().currentUser?.username }
            expect(setOf("admin")) { InMemoryLoginService.get().currentUserRoles }
        }
    }
    @Nested inner class logout {
        @Test fun `clears the logged-in user`() {
            InMemoryLoginService.get().login("admin", "admin")
            InMemoryLoginService.get().logout()
            expect(false) { InMemoryLoginService.get().isLoggedIn }
            expect(null) { InMemoryLoginService.get().currentUser }
            expect(null) { InMemoryLoginService.get().currentPrincipal }
            expect(setOf()) { InMemoryLoginService.get().currentUserRoles }
        }
        @Test fun `succeeds even when nobody is logged in`() {
            InMemoryLoginService.get().logout()
            expect(false) { InMemoryLoginService.get().isLoggedIn }
        }
    }
    @Nested inner class isUserInRole {
        @Test fun `false when not logged in`() {
            expect(false) { InMemoryLoginService.get().isUserInRole("admin") }
        }
        @Test fun `true only for the roles of the logged-in user`() {
            InMemoryLoginService.get().login("admin", "admin")
            expect(true) { InMemoryLoginService.get().isUserInRole("admin") }
            expect(false) { InMemoryLoginService.get().isUserInRole("user") }
        }
    }
}
