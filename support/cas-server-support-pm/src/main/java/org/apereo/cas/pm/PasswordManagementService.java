package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordManagementService {

    /**
     * Execute op to change password.
     *
     * @param c    the credentials
     * @param bean the bean
     * @return true /false
     */
    default boolean change(final Credential c, final PasswordChangeBean bean) {
        return false;
    }

    /**
     * Find email associated with username.
     *
     * @param username the username
     * @return the string
     */
    default String findEmail(final String username) {
        return null;
    }

    /**
     * Create token string.
     *
     * @param username the username
     * @return the string
     */
    default String createToken(final String username) {
        return null;
    }

    /**
     * Parse token string.
     *
     * @param token the token
     * @return the username
     */
    default String parseToken(final String token) {
        return null;
    }

    /**
     * Gets security questions.
     *
     * @param username the username
     * @return the security questions
     */
    default Map<String, String> getSecurityQuestions(final String username) {
        return new LinkedHashMap<>();
    }
}
