package com.github.mvysny.vaadinsimplesecurity.inmemory

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

class InMemoryUserTest : DynaTest({
    test("smoke") {
        InMemoryUser("foo", "bar", setOf("admin", "user"))
    }
    test("equals") {
        expect(InMemoryUser("foo", "bar", setOf("admin", "user"))) {
            InMemoryUser("foo", "bar", setOf("admin", "user"))
        }
        expect(false) {
            InMemoryUser("foo", "bar", setOf("admin", "user")) == InMemoryUser("foo2", "bar", setOf("admin", "user"))
        }
    }
})
