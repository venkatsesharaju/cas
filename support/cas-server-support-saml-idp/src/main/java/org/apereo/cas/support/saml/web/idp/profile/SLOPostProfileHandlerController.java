package org.apereo.cas.support.saml.web.idp.profile;


import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * This is {@link SLOPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SLOPostProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SLOPostProfileHandlerController.class);
    
    /**
     * Instantiates a new slo saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
     * @param authenticationSystemSupport                  the authentication system support
     * @param servicesManager                              the services manager
     * @param webApplicationServiceFactory                 the web application service factory
     * @param samlRegisteredServiceCachingMetadataResolver the saml registered service caching metadata resolver
     * @param configBean                                   the config bean
     * @param responseBuilder                              the response builder
     * @param authenticationContextClassMappings           the authentication context class mappings
     * @param serverPrefix                                 the server prefix
     * @param serverName                                   the server name
     * @param authenticationContextRequestParameter        the authentication context request parameter
     * @param loginUrl                                     the login url
     * @param logoutUrl                                    the logout url
     * @param forceSignedLogoutRequests                    the force signed logout requests
     * @param singleLogoutCallbacksDisabled                the single logout callbacks disabled
     */
    public SLOPostProfileHandlerController(final SamlObjectSigner samlObjectSigner,
                                           final ParserPool parserPool,
                                           final AuthenticationSystemSupport authenticationSystemSupport,
                                           final ServicesManager servicesManager,
                                           final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                           final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                           final OpenSamlConfigBean configBean,
                                           final SamlProfileObjectBuilder<Response> responseBuilder,
                                           final Set<String> authenticationContextClassMappings,
                                           final String serverPrefix,
                                           final String serverName,
                                           final String authenticationContextRequestParameter,
                                           final String loginUrl,
                                           final String logoutUrl,
                                           final boolean forceSignedLogoutRequests,
                                           final boolean singleLogoutCallbacksDisabled) {
        super(samlObjectSigner,
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean,
                responseBuilder,
                authenticationContextClassMappings,
                serverPrefix,
                serverName,
                authenticationContextRequestParameter,
                loginUrl,
                logoutUrl,
                forceSignedLogoutRequests,
                singleLogoutCallbacksDisabled);
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleSloPostProfileRequest(response, request, new HTTPPostDecoder());
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleSloPostProfileRequest(final HttpServletResponse response,
                                               final HttpServletRequest request,
                                               final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        if (singleLogoutCallbacksDisabled) {
            LOGGER.info("Processing SAML IdP SLO requests is disabled");
            return;
        }

        final Pair<? extends SignableSAMLObject, MessageContext> pair = decodeSamlContextFromHttpRequest(request, decoder, LogoutRequest.class);
        final LogoutRequest logoutRequest = LogoutRequest.class.cast(pair.getKey());
        final MessageContext ctx = pair.getValue();

        if (this.forceSignedLogoutRequests && !SAMLBindingSupport.isMessageSigned(ctx)) {
            throw new SAMLException("Logout request is not signed but should be.");
        } else if (SAMLBindingSupport.isMessageSigned(ctx)) {
            final MetadataResolver resolver = SamlIdPUtils.getMetadataResolverForAllSamlServices(this.servicesManager,
                    SamlIdPUtils.getIssuerFromSamlRequest(logoutRequest),
                    this.samlRegisteredServiceCachingMetadataResolver);
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(logoutRequest, resolver, request, ctx);
        }
        SamlUtils.logSamlObject(this.configBean, logoutRequest);
        response.sendRedirect(this.logoutUrl);
    }
}
