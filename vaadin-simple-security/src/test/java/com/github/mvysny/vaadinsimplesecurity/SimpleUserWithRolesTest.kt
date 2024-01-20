package com.github.mvysny.vaadinsimplesecurity

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

class SimpleUserWithRolesTest : DynaTest({
    test("smoke") {
        SimpleUserWithRoles("foo", setOf())
        SimpleUserWithRoles("foo", setOf("bar"))
        SimpleUserWithRoles("foo", null)
    }
    test("equals") {
        expect(SimpleUserWithRoles("foo", setOf())) { SimpleUserWithRoles("foo", setOf()) }
        expect(false) { SimpleUserWithRoles("foo", setOf()) == SimpleUserWithRoles("bar", setOf()) }
    }
    test("hasRole") {
        expect(false) { SimpleUserWithRoles("foo", setOf()).hasRole("foo") }
        expect(false) { SimpleUserWithRoles("foo", null).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("foo") }
        expect(true) { SimpleUserWithRoles("foo", setOf("foo", "bar")).hasRole("bar") }
    }
})
