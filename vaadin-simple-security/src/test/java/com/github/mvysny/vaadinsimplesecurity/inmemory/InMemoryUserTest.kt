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
    }
}
