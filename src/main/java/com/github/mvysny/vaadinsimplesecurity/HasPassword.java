package com.github.mvysny.vaadinsimplesecurity;

import com.github.mvysny.vaadinsimplesecurity.util.PasswordHash;
import org.jetbrains.annotations.NotNull;

/**
 * This mixin interface makes sure that the database-stored passwords are properly hashed and not stored in plaintext.
 * This makes it impossible to guess the user's password even if the database gets compromised.
 * <p></p>
 * Simply create an `User` entity and make it implement this interface. The {@link #getHashedPassword()} field should be stored in the database
 * as-is: it is the user's database hashed and salted and there is no practical way to obtain the original password from it.
 * When the user registers, simply call {@link #setPassword(String)} with the user-provided password.
 * The password will be hashed and the {@link #setHashedPassword(String)} field will be populated.
 * <p></p>
 * After the registration, when the user tries to log in, simply call {@link #passwordMatches(String)} with the user-provided password, to check whether
 * the user provided a correct password or not.
 * <p></p>
 * You can see the example of this mixin interface in the <code>InMemoryUser</code> example user class.
 */
public interface HasPassword {
    /**
     * Retrieves the hashed password from a bean. This field should be stored in the database
     * as-is: it is the user's database hashed and salted and there is no practical way to obtain the original password from it.
     * @return the hashed password.
     */
    @NotNull
    String getHashedPassword();

    /**
     * Sets a new hashed password. You do not have to call this function - will be called automatically by {@link #setPassword(String)}.
     * @param hashedPassword the new hashed password to be stored in the database.
     */
    void setHashedPassword(@NotNull String hashedPassword);

    /**
     * Checks if the password provided by the user at login matches with whatever password user provided during the registration.
     * @param password the password provided by the user at login.
     */
    default boolean passwordMatches(@NotNull String password) {
        return PasswordHash.validatePassword(password, getHashedPassword());
    }

    /**
     * When the user attempts to change the password, or a new user is created, call this function with the user-provided password;
     * the function will in turn call {@link #setHashedPassword(String)}.
     * @param password the new password
     */
    default void setPassword(@NotNull String password) {
        setHashedPassword(PasswordHash.createHash(password));
    }
}
