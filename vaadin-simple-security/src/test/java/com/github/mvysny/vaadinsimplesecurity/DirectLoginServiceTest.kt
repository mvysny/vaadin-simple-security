package com.github.mvysny.vaadinsimplesecurity

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import org.junit.jupiter.api.*
import kotlin.test.expect

class DirectLoginServiceTest {
    companion object {
        private lateinit var routes: Routes
        @BeforeAll @JvmStatic fun setup() {
            routes = Routes().autoDiscoverViews("com.github.mvysny.vaadinsimplesecurity")
        }
    }
    @BeforeEach fun setupVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun teardownVaadin() { MockVaadin.tearDown() }
    @Nested inner class isLoggedIn() {
        @Test fun `false by default`() {
            expect(false) { DirectLoginService.get().isLoggedIn }
        }
        @Test fun `sets to true after successful login`() {
            DirectLoginService.get().login("admin", setOf("admin"))
            expect(true) { DirectLoginService.get().isLoggedIn }
        }
    }
    @Nested inner class login {
        @Test fun succeeds() {
            DirectLoginService.get().login("admin", setOf("admin"))
            expect("admin") { DirectLoginService.get().currentUser?.username }
            expect(setOf("admin")) { DirectLoginService.get().currentUserRoles }
            expect("admin") { DirectLoginService.get().currentPrincipal?.username }
        }
    }
}
