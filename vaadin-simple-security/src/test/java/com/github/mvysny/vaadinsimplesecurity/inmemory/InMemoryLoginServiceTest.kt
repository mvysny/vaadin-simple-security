package com.github.mvysny.vaadinsimplesecurity.inmemory

import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
            expectThrows<FailedLoginException> {
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
}
