package org.apereo.cas.web;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.pac4j.core.context.J2EContext;

import java.util.Set;

/**
 * This is {@link OidcConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcConsentApprovalViewResolver extends OAuth20ConsentApprovalViewResolver {

    @Override
    protected boolean isConsentApprovalBypassed(final J2EContext context, final OAuthRegisteredService service) {
        final String url = context.getFullRequestURL();
        final Set<String> prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompts.contains(OidcConstants.PROMPT_CONSENT)) {
            return false;
        }
        return super.isConsentApprovalBypassed(context, service);
    }
}
