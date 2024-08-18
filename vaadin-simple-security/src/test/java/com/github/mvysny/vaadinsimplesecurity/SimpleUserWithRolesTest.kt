package com.github.mvysny.vaadinsimplesecurity

import org.junit.jupiter.api.Test
import kotlin.test.expect

class SimpleUserWithRolesTest {
    @Test fun smoke() {
        SimpleUserWithRoles("foo", setOf())
        SimpleUserWithRoles("foo", setOf("bar"))
        SimpleUserWithRoles("foo", null)
    }
    @Test fun testEquals() {
        expect(SimpleUserWithRoles("foo", setOf())) { SimpleUserWithRoles("foo", setOf()) }
        expect(false) { SimpleUserWithRoles("foo", setOf()) == SimpleUserWithRoles("bar", setOf()) }
    }
    @Test fun hasRole() {
        expect(false) { SimpleUserWithRoles("foo", setOf()).hasRole("foo") }
        expect(false) { SimpleUserWithRoles("foo", null).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("bar") }
    }
}
