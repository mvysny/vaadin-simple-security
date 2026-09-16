package com.github.mvysny.vaadinsimplesecurity.inmemory

import org.junit.jupiter.api.Test
import kotlin.test.expect

class InMemoryUserTest {
    @Test fun smoke() {
        InMemoryUser("foo", "bar", setOf("admin", "user"))
    }
    @Test fun testEquals() {
        expect(InMemoryUser("foo", "bar", setOf("admin", "user"))) {
            InMemoryUser("foo", "bar", setOf("admin", "user"))
        }
        expect(false) {
            InMemoryUser("foo", "bar", setOf("admin", "user")) == InMemoryUser("foo2", "bar", setOf("admin", "user"))
        }
        val user = InMemoryUser("foo", "bar", setOf())
        val sameInstance: Any = user
        expect(true) { user == sameInstance }  // reflexivity, required of equals()
        expect(false) { user.equals(null) }
        expect(false) { user.equals("foo") }
    }
    @Test fun `password and roles are ignored by equality`() {
        // the username alone identifies the user.
        expect(InMemoryUser("foo", "bar", setOf("admin"))) { InMemoryUser("foo", "baz", setOf("user")) }
        expect(InMemoryUser("foo", "bar", setOf("admin")).hashCode()) { InMemoryUser("foo", "baz", setOf("user")).hashCode() }
    }
    @Test fun testHashCode() {
        expect(InMemoryUser("foo", "bar", setOf()).hashCode()) { InMemoryUser("foo", "bar", setOf()).hashCode() }
        expect(false) { InMemoryUser("foo", "bar", setOf()).hashCode() == InMemoryUser("foo2", "bar", setOf()).hashCode() }
    }
    @Test fun testToString() {
        expect("InMemoryUser{'foo', roles=[admin]}") { InMemoryUser("foo", "bar", setOf("admin")).toString() }
    }
    @Test fun `getRoles hands out a defensive copy`() {
        val user = InMemoryUser("foo", "bar", setOf("admin"))
        user.roles.clear()
        expect(setOf("admin")) { user.roles }
    }
    @Test fun `the password is stored hashed`() {
        val user = InMemoryUser("foo", "bar", setOf())
        expect(false) { user.hashedPassword!!.contains("bar") }
        expect(true) { user.passwordMatches("bar") }
    }
}
