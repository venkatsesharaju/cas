package org.apereo.cas.support.oauth.validator;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Validate OAuth inputs.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuth20Validator {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    private ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    public OAuth20Validator(final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory) {
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
    }

    /**
     * Check if a parameter exists.
     *
     * @param request the HTTP request
     * @param name    the parameter name
     * @return whether the parameter exists
     */
    public boolean checkParameterExist(final HttpServletRequest request, final String name) {
        final String parameter = request.getParameter(name);
        logger.debug("{}: {}", name, parameter);
        if (StringUtils.isBlank(parameter)) {
            logger.error("Missing: {}", name);
            return false;
        }
        return true;
    }

    /**
     * Check if the service is valid.
     *
     * @param registeredService the registered service
     * @return whether the service is valid
     */
    public boolean checkServiceValid(final RegisteredService registeredService) {
        if (registeredService == null) {
            return false;
        }

        final WebApplicationService service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
        logger.debug("Check registered service: {}", registeredService);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            return true;
        } catch (final UnauthorizedServiceException e) {
            return false;
        }
    }

    /**
     * Check if the callback url is valid.
     *
     * @param registeredService the registered service
     * @param redirectUri       the callback url
     * @return whether the callback url is valid
     */
    public boolean checkCallbackValid(final RegisteredService registeredService, final String redirectUri) {
        final String registeredServiceId = registeredService.getServiceId();
        logger.debug("Found: {} vs redirectUri: {}", registeredService, redirectUri);
        if (!redirectUri.matches(registeredServiceId)) {
            logger.error("Unsupported {}: {} for registeredServiceId: {}", OAuthConstants.REDIRECT_URI, redirectUri, registeredServiceId);
            return false;
        }
        return true;
    }

    /**
     * Check the client secret.
     *
     * @param registeredService the registered service
     * @param clientSecret      the client secret
     * @return whether the secret is valid
     */
    public boolean checkClientSecret(final OAuthRegisteredService registeredService, final String clientSecret) {
        logger.debug("Found: {} in secret check", registeredService);
        if (!StringUtils.equals(registeredService.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service: {}", registeredService);
            return false;
        }
        return true;
    }
}
