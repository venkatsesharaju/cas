package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.InetAddressUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlAuthNStatementBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<AuthnStatement> {

    private static final long serialVersionUID = 8761566449790497226L;

    @Autowired
    private CasConfigurationProperties casProperties;

    private final AuthnContextClassRefBuilder authnContextClassRefBuilder;

    public SamlProfileSamlAuthNStatementBuilder(final OpenSamlConfigBean configBean,
                                                final AuthnContextClassRefBuilder authnContextClassRefBuilder) {
        super(configBean);
        this.authnContextClassRefBuilder = authnContextClassRefBuilder;
    }

    @Override
    public AuthnStatement build(final AuthnRequest authnRequest,
                                final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Assertion assertion,
                                final SamlRegisteredService service,
                                final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                final String binding) throws SamlException {
        return buildAuthnStatement(assertion, authnRequest, adaptor, service, binding);
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param service      the service
     * @param binding      the binding
     * @return constructed authentication statement
     * @throws SamlException the saml exception
     */
    private AuthnStatement buildAuthnStatement(final Assertion assertion, final AuthnRequest authnRequest,
                                               final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                               final SamlRegisteredService service, final String binding) throws SamlException {

        final String authenticationMethod = this.authnContextClassRefBuilder.build(assertion, authnRequest, adaptor, service);
        final String id = '_' + String.valueOf(Math.abs(new SecureRandom().nextLong()));
        final AuthnStatement statement = newAuthnStatement(authenticationMethod, DateTimeUtils.zonedDateTimeOf(assertion.getAuthenticationDate()), id);
        if (assertion.getValidUntilDate() != null) {
            final ZonedDateTime dt = DateTimeUtils.zonedDateTimeOf(assertion.getValidUntilDate());
            statement.setSessionNotOnOrAfter(
                    DateTimeUtils.dateTimeOf(dt.plusSeconds(casProperties.getAuthn().getSamlIdp().getResponse().getSkewAllowance())));
        }
        statement.setSubjectLocality(buildSubjectLocality(assertion, authnRequest, adaptor, binding));
        return statement;
    }

    /**
     * Build subject locality subject locality.
     *
     * @param assertion    the assertion
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @param binding      the binding
     * @return the subject locality
     * @throws SamlException the saml exception
     */
    protected SubjectLocality buildSubjectLocality(final Assertion assertion, final AuthnRequest authnRequest,
                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                   final String binding)
            throws SamlException {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        final AssertionConsumerService acs = adaptor.getAssertionConsumerService(binding);
        if (acs != null && StringUtils.isNotBlank(acs.getLocation())) {
            final String ip = InetAddressUtils.getByName(acs.getLocation());
            if (StringUtils.isNotBlank(ip)) {
                subjectLocality.setAddress(ip);
            }
        }
        return subjectLocality;
    }
}
