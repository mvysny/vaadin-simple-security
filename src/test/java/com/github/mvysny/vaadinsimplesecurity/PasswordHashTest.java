package com.github.mvysny.vaadinsimplesecurity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashTest {
    @Test
    public void testMatchingPassword() throws Exception {
        assertTrue(PasswordHash.validatePassword("foo", PasswordHash.createHash("foo")));
    }

    @Test
    public void testNonMatchingPassword() throws Exception {
        assertFalse(
                PasswordHash.validatePassword("bar", PasswordHash.createHash("foo"))
        );
    }

    @Test
    public void testDifferentSalt() throws Exception {
        final String hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".getBytes());
        String hash2 = PasswordHash.createHash("foo".toCharArray(), "salt2".getBytes());
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testDifferentPassword() throws Exception {
        String hash1 = PasswordHash.createHash("foo".toCharArray(), "salt1".getBytes());
        String hash2 = PasswordHash.createHash("bar".toCharArray(), "salt1".getBytes());
        assertNotEquals(hash1, hash2);
    }
}
