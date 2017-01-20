package org.apereo.cas;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An abstract implementation of the {@link CentralAuthenticationService} that provides access to
 * the needed scaffolding and services that are necessary to CAS, such as ticket registry, service registry, etc.
 * The intention here is to allow extensions to easily benefit these already-configured components
 * without having to to duplicate them again.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService, Serializable,
                                                                      ApplicationEventPublisherAware {

    private static final long serialVersionUID = -7572316677901391166L;

    /**
     * Log instance for logging events, info, warnings, errors, etc.
     */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Application event publisher.
     */
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@link TicketRegistry}  for storing and retrieving tickets as needed.
     */
    protected TicketRegistry ticketRegistry;

    /**
     * Implementation of Service Manager.
     */
    protected ServicesManager servicesManager;

    /**
     * The logout manager.
     **/
    protected LogoutManager logoutManager;

    /**
     * The ticket factory.
     **/
    protected TicketFactory ticketFactory;

    /**
     * The service selection strategy during validation events.
     **/
    protected List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    /**
     * Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     */
    protected ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory = new AcceptAnyAuthenticationPolicyFactory();

    /**
     * Factory to create the principal type.
     **/
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /**
     * Cipher executor to handle ticket validation.
     */
    protected CipherExecutor<String, String> cipherExecutor;

    /**
     * Build the central authentication service implementation.
     *  @param ticketRegistry  the tickets registry.
     * @param ticketFactory   the ticket factory
     * @param servicesManager the services manager.
     * @param logoutManager   the logout manager.
     * @param authenticationRequestServiceSelectionStrategies The service selection strategy during validation events.
     * @param authenticationPolicyFactory Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     * @param principalFactory principal factory to create principal objects
     * @param cipherExecutor Cipher executor to handle ticket validation.
     */
    public AbstractCentralAuthenticationService(final TicketRegistry ticketRegistry, final TicketFactory ticketFactory, final ServicesManager servicesManager,
             final LogoutManager logoutManager, final List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies,
             final ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory, final PrincipalFactory principalFactory,
             final CipherExecutor<String, String> cipherExecutor) {
        this.ticketRegistry = ticketRegistry;
        this.servicesManager = servicesManager;
        this.logoutManager = logoutManager;
        this.ticketFactory = ticketFactory;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.principalFactory = principalFactory;
        this.serviceContextAuthenticationPolicyFactory = authenticationPolicyFactory;
        this.cipherExecutor = cipherExecutor;
    }
    /**
     * Publish CAS events.
     *
     * @param e the event
     */
    protected void doPublishEvent(final ApplicationEvent e) {
        if (applicationEventPublisher != null) {
            logger.debug("Publishing {}", e);
            this.applicationEventPublisher.publishEvent(e);
        }
    }

    @Transactional(readOnly = true, transactionManager = "ticketTransactionManager",
            noRollbackFor = InvalidTicketException.class)
    @Timed(name = "GET_TICKET_TIMER")
    @Metered(name = "GET_TICKET_METER")
    @Counted(name = "GET_TICKET_COUNTER", monotonic = true)
    @Override
    public <T extends Ticket> T getTicket(final String ticketId) throws InvalidTicketException {
        Assert.notNull(ticketId, "ticketId cannot be null");
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId);
        verifyTicketState(ticket, ticketId, null);
        return (T) ticket;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note:
     * Synchronization on ticket object in case of cache based registry doesn't serialize
     * access to critical section. The reason is that cache pulls serialized data and
     * builds new object, most likely for each pull. Is this synchronization needed here?
     */
    @Transactional(readOnly = true, transactionManager = "ticketTransactionManager",
            noRollbackFor = InvalidTicketException.class)
    @Timed(name = "GET_TICKET_TIMER")
    @Metered(name = "GET_TICKET_METER")
    @Counted(name = "GET_TICKET_COUNTER", monotonic = true)
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<T> clazz) throws InvalidTicketException {
        Assert.notNull(ticketId, "ticketId cannot be null");
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId, clazz);
        verifyTicketState(ticket, ticketId, clazz);
        return (T) ticket;
    }

    @Transactional(readOnly = true, transactionManager = "ticketTransactionManager")
    @Timed(name = "GET_TICKETS_TIMER")
    @Metered(name = "GET_TICKETS_METER")
    @Counted(name = "GET_TICKETS_COUNTER", monotonic = true)
    @Override
    public Collection<Ticket> getTickets(final Predicate<Ticket> predicate) {
        return this.ticketRegistry.getTickets().stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the authentication satisfied by policy.
     *
     * @param authentication the authentication
     * @param context        the context
     * @return the authentication satisfied by policy
     * @throws AbstractTicketException the ticket exception
     */
    protected Authentication getAuthenticationSatisfiedByPolicy(final Authentication authentication, final ServiceContext context)
            throws AbstractTicketException {

        final ContextualAuthenticationPolicy<ServiceContext> policy = this.serviceContextAuthenticationPolicyFactory.createPolicy(context);
        if (policy.isSatisfiedBy(authentication)) {
            return authentication;
        }
        throw new UnsatisfiedAuthenticationPolicyException(policy);
    }

    /**
     * Evaluate proxied service if needed.
     *
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param registeredService    the registered service
     */
    protected void evaluateProxiedServiceIfNeeded(final Service service, final TicketGrantingTicket ticketGrantingTicket,
                                                  final RegisteredService registeredService) {
        final Service proxiedBy = ticketGrantingTicket.getProxiedBy();
        if (proxiedBy != null) {
            logger.debug("TGT is proxied by [{}]. Locating proxy service in registry...", proxiedBy.getId());
            final RegisteredService proxyingService = this.servicesManager.findServiceBy(proxiedBy);

            if (proxyingService != null) {
                logger.debug("Located proxying service [{}] in the service registry", proxyingService);
                if (!proxyingService.getProxyPolicy().isAllowedToProxy()) {
                    logger.warn("Found proxying service {}, but it is not authorized to fulfill the proxy attempt made by {}",
                            proxyingService.getId(), service.getId());
                    throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE
                            + registeredService.getId());
                }
            } else {
                logger.warn("No proxying service found. Proxy attempt by service [{}] (registered service [{}]) is not allowed.",
                        service.getId(), registeredService.getId());
                throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE
                        + registeredService.getId());
            }
        } else {
            logger.trace("TGT is not proxied by another service");
        }
    }

    /**
     * Validate ticket expiration policy and throws exception if ticket is no longer valid.
     * Expired tickets are also deleted from the registry immediately on demand.
     *
     * @param ticket the ticket
     * @param id     the original id
     * @param clazz  the clazz
     */
    protected void verifyTicketState(final Ticket ticket, final String id, final Class clazz) {
        if (ticket == null) {
            logger.debug("Ticket [{}] by type [{}] cannot be found in the ticket registry.", id,
                    clazz != null ? clazz.getSimpleName() : "unspecified");
            throw new InvalidTicketException(id);
        }
        synchronized (ticket) {
            if (ticket.isExpired()) {
                this.ticketRegistry.deleteTicket(id);
                logger.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticket);
                throw new InvalidTicketException(id);
            }
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        this.ticketRegistry.updateTicket(ticket);
        return ticket;
    }

    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final Service service) {
        return this.authenticationRequestServiceSelectionStrategies.stream()
                .sorted()
                .filter(s -> s.supports(service))
                .findFirst()
                .get()
                .resolveServiceFrom(service);
    }

    /**
     * Verify the ticket id received is actually legitimate
     * before contacting downstream systems to find and process it.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    protected boolean isTicketAuthenticityVerified(final String ticketId) {
        if (this.cipherExecutor != null) {
            logger.debug("Attempting to decode service ticket {} to verify authenticity", ticketId);
            return !StringUtils.isEmpty(this.cipherExecutor.decode(ticketId));
        }
        return !StringUtils.isEmpty(ticketId);
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
