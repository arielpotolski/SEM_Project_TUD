package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a password in our domain.
 */
@EqualsAndHashCode
public class Password {
    private final transient String passwordValue;

    public Password(String password) {
        if(password.length() > 20 || password.length() < 6) {
            throw new IllegalArgumentException("Length of password is incorrect");
        }
        this.passwordValue = password;
    }

    @Override
    public String toString() {
        return passwordValue;
    }
}
