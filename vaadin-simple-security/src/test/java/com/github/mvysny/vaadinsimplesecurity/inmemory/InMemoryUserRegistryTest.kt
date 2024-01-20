package com.github.mvysny.vaadinsimplesecurity.inmemory

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

class InMemoryUserRegistryTest : DynaTest({
    beforeEach { InMemoryUserRegistry.get().clear() }
    afterEach { InMemoryUserRegistry.get().clear() }
    test("smoke") {
        InMemoryUserRegistry.get().registerUser(InMemoryUser("foo", "bar", setOf()))
        expect("foo") { InMemoryUserRegistry.get().findByUsername("foo")?.username }
    }
})
