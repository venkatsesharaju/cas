package org.apereo.cas.tokens;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.DefaultServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.TokenTicketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is {@link JWTServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTServiceTicketResourceEntityResponseFactory extends DefaultServiceTicketResourceEntityResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTServiceTicketResourceEntityResponseFactory.class);

    /**
     * The ticket builder that produces tokens.
     */
    private final TokenTicketBuilder tokenTicketBuilder;

    private final ServicesManager servicesManager;

    public JWTServiceTicketResourceEntityResponseFactory(final CentralAuthenticationService centralAuthenticationService,
                                                         final TokenTicketBuilder tokenTicketBuilder,
                                                         final ServicesManager servicesManager) {
        super(centralAuthenticationService);
        this.tokenTicketBuilder = tokenTicketBuilder;
        this.servicesManager = servicesManager;
    }

    @Override
    protected String grantServiceTicket(final String ticketGrantingTicket, final Service service,
                                        final AuthenticationResult authenticationResult) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        LOGGER.debug("Located registered service [{}] for [{}]", registeredService, service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        final Map.Entry<String, RegisteredServiceProperty> property = registeredService.getProperties()
                .entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(TokenConstants.PROPERTY_NAME_TOKEN_AS_RESPONSE)
                        && BooleanUtils.toBoolean(entry.getValue().getValue()))
                .distinct()
                .findFirst()
                .orElse(null);

        if (property == null) {
            LOGGER.debug("Service [{}] does not require JWTs as tickets", service);
            return super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        }

        final String serviceTicket = super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        final String jwt = this.tokenTicketBuilder.build(serviceTicket, service);
        LOGGER.debug("Generated JWT [{}] for service [{}]", jwt, service);
        return jwt;
    }
}
