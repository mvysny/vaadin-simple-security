package com.github.mvysny.vaadinsimplesecurity

import org.junit.jupiter.api.Test
import kotlin.test.expect

class SimpleUserWithRolesTest {
    @Test fun smoke() {
        SimpleUserWithRoles("foo", setOf())
        SimpleUserWithRoles("foo", setOf("bar"))
        SimpleUserWithRoles("foo", null)
    }
    @Test fun `no roles when constructed with the username alone`() {
        expect(setOf()) { SimpleUserWithRoles("foo").roles }
        expect(false) { SimpleUserWithRoles("foo").hasRole("foo") }
    }
    @Test fun `username is the Principal name`() {
        expect("foo") { SimpleUserWithRoles("foo", setOf("bar")).username }
        expect("foo") { SimpleUserWithRoles("foo", setOf("bar")).name }
    }
    @Test fun testToString() {
        expect("SimpleUserWithRoles{foo'}") { SimpleUserWithRoles("foo", setOf("bar")).toString() }
    }
    @Test fun testEquals() {
        expect(SimpleUserWithRoles("foo", setOf())) { SimpleUserWithRoles("foo", setOf()) }
        expect(false) { SimpleUserWithRoles("foo", setOf()) == SimpleUserWithRoles("bar", setOf()) }
        val user = SimpleUserWithRoles("foo", setOf())
        val sameInstance: Any = user
        expect(true) { user == sameInstance }  // reflexivity, required of equals()
        expect(false) { user.equals(null) }
        expect(false) { user.equals("foo") }
    }
    @Test fun `roles are ignored by equality`() {
        // the username alone identifies the user.
        expect(SimpleUserWithRoles("foo", setOf("admin"))) { SimpleUserWithRoles("foo", setOf("user")) }
        expect(SimpleUserWithRoles("foo", setOf("admin")).hashCode()) { SimpleUserWithRoles("foo", setOf("user")).hashCode() }
    }
    @Test fun testHashCode() {
        expect(SimpleUserWithRoles("foo", setOf()).hashCode()) { SimpleUserWithRoles("foo", setOf()).hashCode() }
        expect(false) { SimpleUserWithRoles("foo", setOf()).hashCode() == SimpleUserWithRoles("bar", setOf()).hashCode() }
    }
    @Test fun hasRole() {
        expect(false) { SimpleUserWithRoles("foo", setOf()).hasRole("foo") }
        expect(false) { SimpleUserWithRoles("foo", null).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("bar") }
    }
}
