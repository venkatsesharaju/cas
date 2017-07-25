package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.spi.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.SurrogateAuthenticationMetaDataPopulator;

/**
 * This is {@link SurrogatePrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogatePrincipalIdProvider implements AuditPrincipalIdProvider {
    @Override
    public String getPrincipalIdFrom(final Authentication authentication) {
        if (authentication == null) {
            return "unknown";
        }
        if (authentication.getAttributes().containsKey(SurrogateAuthenticationMetaDataPopulator.AUTHENTICATION_ATTR_SURROGATE_USER)) {
            final String surrogateUser = authentication.getAttributes()
                    .get(SurrogateAuthenticationMetaDataPopulator.AUTHENTICATION_ATTR_SURROGATE_USER).toString();
            final String principalId = authentication.getAttributes()
                    .get(SurrogateAuthenticationMetaDataPopulator.AUTHENTICATION_ATTR_SURROGATE_CREDENTIAL).toString();
            return String.format("(Real user: [%s], Surrogate user: [%s])", principalId, surrogateUser);
        }
        return authentication.getPrincipal().getId();
    }
}
