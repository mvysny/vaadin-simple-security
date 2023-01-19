package com.github.mvysny.vaadinsimplesecurity.util;

import org.junit.jupiter.api.Test;

import static com.github.mvysny.vaadinsimplesecurity.util.PasswordHash.createHash;
import static com.github.mvysny.vaadinsimplesecurity.util.PasswordHash.validatePassword;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {
    @Test
    public void testMatchingPassword() {
        assertTrue(validatePassword("foo", createHash("foo")));
    }

    @Test
    public void testNonMatchingPassword() {
        assertFalse(validatePassword("bar", createHash("foo")));
    }

    @Test
    public void testDifferentSalt() {
        final String hash1 = createHash("foo".toCharArray(), "salt1".getBytes());
        String hash2 = createHash("foo".toCharArray(), "salt2".getBytes());
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testDifferentPassword() {
        String hash1 = createHash("foo".toCharArray(), "salt1".getBytes());
        String hash2 = createHash("bar".toCharArray(), "salt1".getBytes());
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void originalPasswordHashTest() {
        // Print out 10 hashes
        for (int i = 0; i < 10; i++)
            System.out.println(createHash("p\r\nassw0Rd!"));

        // Test password validation
        for (int i = 0; i < 10; i++) {
            String password = "" + i;
            String hash = createHash(password);
            String secondHash = createHash(password);
            assertNotEquals(hash, secondHash);
            String wrongPassword = "" + (i + 1);
            assertFalse(validatePassword(wrongPassword, hash), "FAILURE: WRONG PASSWORD ACCEPTED!");
            assertTrue(validatePassword(password, hash), "FAILURE: GOOD PASSWORD NOT ACCEPTED!");
        }
    }
}
