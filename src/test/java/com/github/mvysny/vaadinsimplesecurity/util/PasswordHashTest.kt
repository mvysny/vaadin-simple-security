package com.github.mvysny.vaadinsimplesecurity.util

import com.github.mvysny.dynatest.DynaTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.expect

class PasswordHashTest : DynaTest({
    test("matching password") {
        expect(true) {
            PasswordHash.validatePassword("foo", PasswordHash.createHash("foo"))
        }
    }

    test("non-matching password") {
        expect(false) {
            PasswordHash.validatePassword("bar", PasswordHash.createHash("foo"))
        }
    }

    test("different salt") {
        val hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".toByteArray())
        val hash2 = PasswordHash.createHash("foo".toCharArray(), "salt2".toByteArray())
        expect(false, "$hash1, $hash2") { hash1 == hash2 }
    }

    test("different password") {
        val hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".toByteArray())
        val hash2 = PasswordHash.createHash("bar".toCharArray(), "salt1".toByteArray())
        expect(false, "$hash1, $hash2") { hash1 == hash2 }
    }


    test("originalPasswordHashTest") {
        // Print out 10 hashes
        for (i in 0..9) println(PasswordHash.createHash("p\r\nassw0Rd!"))

        // Test password validation
        for (i in 0..9) {
            val password = "" + i
            val hash = PasswordHash.createHash(password)
            val secondHash = PasswordHash.createHash(password)
            Assertions.assertNotEquals(hash, secondHash)
            val wrongPassword = "" + (i + 1)
            expect(false) { PasswordHash.validatePassword(wrongPassword, hash) }
            expect(true) { PasswordHash.validatePassword(password, hash) }
        }
    }
})
