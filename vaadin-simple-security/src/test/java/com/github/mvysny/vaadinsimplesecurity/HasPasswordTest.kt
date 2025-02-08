package com.github.mvysny.vaadinsimplesecurity

import com.github.mvysny.vaadinsimplesecurity.inmemory.InMemoryUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.expect

class HasPasswordTest {
    @Test
    fun simpleTest() {
        val user = InMemoryUser("foo", "foo", setOf())
        expect(true) { user.passwordMatches("foo") }
        expect(false) { user.passwordMatches("bar") }
    }

    @Test
    fun ssoUser() {
        val user = InMemoryUser("foo", null, setOf())
        expect(false) { user.passwordMatches("foo") }
        expect(false) { user.passwordMatches("bar") }
    }

    @Test
    fun setNullPassword() {
        val user = InMemoryUser("foo", "foo", setOf())
        user.setPassword(null)
        expect(false) { user.passwordMatches("foo") }
        expect(false) { user.passwordMatches("bar") }
    }
}
