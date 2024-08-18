package com.github.mvysny.vaadinsimplesecurity.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.expect

class PasswordHashTest {
    @Test fun matchingPassword() {
        expect(true) {
            PasswordHash.validatePassword("foo", PasswordHash.createHash("foo"))
        }
    }

    @Test fun `non-matching password`() {
        expect(false) {
            PasswordHash.validatePassword("bar", PasswordHash.createHash("foo"))
        }
    }

    @Test fun differentSalt() {
        val hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".toByteArray())
        val hash2 = PasswordHash.createHash("foo".toCharArray(), "salt2".toByteArray())
        expect(false, "$hash1, $hash2") { hash1 == hash2 }
    }

    @Test fun differentPassword() {
        val hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".toByteArray())
        val hash2 = PasswordHash.createHash("bar".toCharArray(), "salt1".toByteArray())
        expect(false, "$hash1, $hash2") { hash1 == hash2 }
    }

    @Test fun originalPasswordHashTest() {
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
}
