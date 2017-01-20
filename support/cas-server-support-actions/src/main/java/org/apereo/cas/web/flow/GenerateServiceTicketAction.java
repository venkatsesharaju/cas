package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to generate a service ticket for a given Ticket Granting Ticket and
 * Service.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class GenerateServiceTicketAction extends AbstractAction {

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final ServicesManager servicesManager;

    public GenerateServiceTicketAction(final AuthenticationSystemSupport authenticationSystemSupport,
                                       final CentralAuthenticationService authenticationService,
                                       final TicketRegistrySupport ticketRegistrySupport,
                                       final ServicesManager servicesManager) {
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.centralAuthenticationService = authenticationService;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.servicesManager = servicesManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the initial primary authentication flow, credentials are cached and available.
     * Since they are authenticated as part of submission first, there is no need to doubly
     * authenticate and verify credentials.
     * <p>
     * In subsequent authentication flows where a TGT is available and only an ST needs to be
     * created, there are no cached copies of the credential, since we do have a TGT available.
     * So we will simply grab the available authentication and produce the final result based on that.
     */
    @Override
    protected Event doExecute(final RequestContext context) {
        final Service service = WebUtils.getService(context);
        final String ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);

        try {

            final Authentication authentication = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
            if (authentication == null) {
                throw new InvalidTicketException(
                        new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket);
            }

            final RegisteredService registeredService = servicesManager.findServiceBy(service);
            WebUtils.putRegisteredService(context, registeredService);
            WebUtils.putService(context, service);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context,
                    registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());

            if (WebUtils.getWarningCookie(context)) {
                return result(CasWebflowConstants.STATE_ID_WARN);
            }

            final AuthenticationResultBuilder authenticationResultBuilder =
                    this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
            final AuthenticationResult authenticationResult = authenticationResultBuilder.build(service);

            final ServiceTicket serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
            WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
            return success();

        } catch (final AbstractTicketException e) {
            if (e instanceof InvalidTicketException) {
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            }
            if (isGatewayPresent(context)) {
                return result(CasWebflowConstants.STATE_ID_GATEWAY);
            }
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }

    /**
     * Checks if {@code gateway} is present in the request params.
     *
     * @param context the context
     * @return true, if gateway present
     */
    protected boolean isGatewayPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
                .getRequestParameterMap().get(CasProtocolConstants.PARAMETER_GATEWAY));
    }

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    private Event newEvent(final String id, final Exception error) {
        return new EventFactorySupport().event(this, id, new LocalAttributeMap<>("error", error));
    }
}
