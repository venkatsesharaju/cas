package org.apereo.cas.web.flow.resolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.StringUtils.*;

/**
 * This is {@link AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver}
 * that attempts to locate an authentication attribute, match its value against
 * the provided pattern and decide the next event in the flow for the given service.
 * This is useful in triggering authentication events based on metadata collected directly
 * by the authentication machinery.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    private final String globalAuthenticationAttributeValueRegex;
    private final Set<String> attributeNames;

    public AuthenticationAttributeMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                               final CentralAuthenticationService centralAuthenticationService,
                                                                               final ServicesManager servicesManager,
                                                                               final TicketRegistrySupport ticketRegistrySupport,
                                                                               final CookieGenerator warnCookieGenerator,
                                                                               final List<AuthenticationRequestServiceSelectionStrategy> selectionStrategies,
                                                                               final MultifactorAuthenticationProviderSelector selector,
                                                                               final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, selectionStrategies,
                selector);
        globalAuthenticationAttributeValueRegex = casProperties.getAuthn().getMfa().getGlobalAuthenticationAttributeValueRegex();
        attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalAuthenticationAttributeNameTriggers());
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        if (attributeNames.isEmpty()) {
            logger.debug("Authentication attribute name to determine event is not configured");
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        final Collection<MultifactorAuthenticationProvider> providers = flattenProviders(providerMap.values());
        if (providers.size() == 1 && StringUtils.isNotBlank(globalAuthenticationAttributeValueRegex)) {
            final MultifactorAuthenticationProvider provider = providers.iterator().next();
            logger.debug("Found a single multifactor provider {} in the application context", provider);
            return resolveEventViaAuthenticationAttribute(authentication, attributeNames, service, context, providers,
                    input -> input != null && input.matches(globalAuthenticationAttributeValueRegex));
        }

        return resolveEventViaAuthenticationAttribute(authentication, attributeNames, service, context, providers,
                input -> providers.stream()
                        .filter(provider -> input != null && provider.matches(input))
                        .count() > 0);
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
