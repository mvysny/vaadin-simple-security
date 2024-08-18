package com.github.mvysny.vaadinsimplesecurity.inmemory

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

class InMemoryUserRegistryTest {
    @BeforeEach @AfterEach fun clearUserRegistry() { InMemoryUserRegistry.get().clear() }
    @Test fun smoke() {
        InMemoryUserRegistry.get().registerUser(InMemoryUser("foo", "bar", setOf()))
        expect("foo") { InMemoryUserRegistry.get().findByUsername("foo")?.username }
    }
}
