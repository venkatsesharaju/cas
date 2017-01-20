package org.apereo.cas.mgmt.config;

import com.google.common.base.Throwables;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.mgmt.services.web.ManageRegisteredServicesMultiActionController;
import org.apereo.cas.mgmt.services.web.RegisteredServiceSimpleFormController;
import org.apereo.cas.mgmt.services.web.factory.AccessStrategyMapper;
import org.apereo.cas.mgmt.services.web.factory.AttributeFilterMapper;
import org.apereo.cas.mgmt.services.web.factory.AttributeFormDataPopulator;
import org.apereo.cas.mgmt.services.web.factory.AttributeReleasePolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultAccessStrategyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultAttributeFilterMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultAttributeReleasePolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultPrincipalAttributesRepositoryMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultProxyPolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultRegisteredServiceFactory;
import org.apereo.cas.mgmt.services.web.factory.DefaultRegisteredServiceMapper;
import org.apereo.cas.mgmt.services.web.factory.DefaultUsernameAttributeProviderMapper;
import org.apereo.cas.mgmt.services.web.factory.FormDataPopulator;
import org.apereo.cas.mgmt.services.web.factory.PrincipalAttributesRepositoryMapper;
import org.apereo.cas.mgmt.services.web.factory.ProxyPolicyMapper;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceFactory;
import org.apereo.cas.mgmt.services.web.factory.RegisteredServiceMapper;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.pac4j.cas.client.direct.DirectCasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator;
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CasManagementWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casManagementWebAppConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasManagementWebAppConfiguration extends WebMvcConfigurerAdapter {
    
    @Autowired(required = false)
    @Qualifier("formDataPopulators")
    private List formDataPopulators = new ArrayList<>();

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Bean
    public Filter characterEncodingFilter() {
        return new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true);
    }

    @Bean
    public Authorizer requireAnyRoleAuthorizer() {
        return new RequireAnyRoleAuthorizer(casProperties.getMgmt().getAdminRoles());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "attributeRepository")
    @Bean(name = {"stubAttributeRepository", "attributeRepository"})
    public IPersonAttributeDao stubAttributeRepository() {
        return Beans.newStubAttributeRepository(casProperties.getAuthn().getAttributeRepository());
    }

    @Bean
    public Client casClient() {
        final CasConfiguration cfg = new CasConfiguration(casProperties.getServer().getLoginUrl());
        final DirectCasClient client = new DirectCasClient(cfg);
        client.setAuthorizationGenerator(authorizationGenerator());
        client.setName("CasClient");
        return client;
    }

    @Bean
    public Config config() {
        final Config cfg = new Config(getDefaultServiceUrl(), casClient());
        cfg.setAuthorizer(requireAnyRoleAuthorizer());
        return cfg;
    }

    @Bean
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                         final HttpServletResponse response)
                    throws Exception {
                final String url = request.getContextPath() + "/manage.html";
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    @Bean
    public SimpleUrlHandlerMapping handlerMappingC() {
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setAlwaysUseFullPath(true);
        mapping.setRootHandler(rootController());

        final Properties properties = new Properties();
        properties.put("/*.html", new UrlFilenameViewController());
        mapping.setMappings(properties);
        return mapping;
    }

    @Bean
    public HandlerInterceptorAdapter casManagementSecurityInterceptor() {
        return new CasManagementSecurityInterceptor();
    }
    
    @RefreshScope
    @Bean
    public Properties userProperties() {
        try {
            final Properties p = new Properties();
            p.load(casProperties.getMgmt().getUserPropertiesFile().getInputStream());
            return p;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @ConditionalOnMissingBean(name = "authorizationGenerator")
    @Bean
    @RefreshScope
    public AuthorizationGenerator authorizationGenerator() {
        final List<String> authzAttributes = casProperties.getMgmt().getAuthzAttributes();
        if (!authzAttributes.isEmpty()) {
            if ("*".equals(authzAttributes)) {
                return commonProfile -> commonProfile.addRoles(casProperties.getMgmt().getAdminRoles());
            }
            return new FromAttributesAuthorizationGenerator(authzAttributes.toArray(new String[]{}), new String[]{});
        }
        return new SpringSecurityPropertiesAuthorizationGenerator(userProperties());
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        return new CookieLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                final Locale locale = request.getLocale();
                if (StringUtils.isEmpty(casProperties.getMgmt().getDefaultLocale())
                        || !locale.getLanguage().equals(casProperties.getMgmt().getDefaultLocale())) {
                    return locale;
                }
                return new Locale(casProperties.getMgmt().getDefaultLocale());
            }
        };
    }

    @RefreshScope
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor bean = new LocaleChangeInterceptor();
        bean.setParamName(this.casProperties.getLocale().getParamName());
        return bean;
    }

   

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(casManagementSecurityInterceptor())
                .addPathPatterns("/**").excludePathPatterns("/callback*", "/logout*", "/authorizationFailure");
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Bean
    public AccessStrategyMapper defaultAccessStrategyMapper() {
        return new DefaultAccessStrategyMapper();
    }

    @Bean
    public RegisteredServiceFactory registeredServiceFactory() {
        this.formDataPopulators.add(attributeFormDataPopulator());
        return new DefaultRegisteredServiceFactory(defaultAccessStrategyMapper(), defaultAttributeReleasePolicyMapper(), defaultProxyPolicyMapper(),
                defaultRegisteredServiceMapper(), usernameAttributeProviderMapper(), formDataPopulators);
    }

    @Bean
    public AttributeReleasePolicyMapper defaultAttributeReleasePolicyMapper() {
        return new DefaultAttributeReleasePolicyMapper(defaultAttributeFilterMapper(), defaultPrincipalAttributesRepositoryMapper());
    }

    @Bean
    public FormDataPopulator attributeFormDataPopulator() {
        return new AttributeFormDataPopulator(stubAttributeRepository());
    }

    @Bean
    public DefaultUsernameAttributeProviderMapper usernameAttributeProviderMapper() {
        return new DefaultUsernameAttributeProviderMapper();
    }

    @Bean
    public RegisteredServiceMapper defaultRegisteredServiceMapper() {
        return new DefaultRegisteredServiceMapper();
    }

    @Bean
    public ProxyPolicyMapper defaultProxyPolicyMapper() {
        return new DefaultProxyPolicyMapper();
    }

    @Bean
    public AttributeFilterMapper defaultAttributeFilterMapper() {
        return new DefaultAttributeFilterMapper();
    }

    @Bean
    public PrincipalAttributesRepositoryMapper defaultPrincipalAttributesRepositoryMapper() {
        return new DefaultPrincipalAttributesRepositoryMapper();
    }

    @Bean
    public ManageRegisteredServicesMultiActionController manageRegisteredServicesMultiActionController(
            @Qualifier("servicesManager") final ServicesManager servicesManager) {

        return new ManageRegisteredServicesMultiActionController(servicesManager, registeredServiceFactory(), webApplicationServiceFactory,
                getDefaultServiceUrl());
    }

    @Bean
    public RegisteredServiceSimpleFormController registeredServiceSimpleFormController(@Qualifier("servicesManager") final ServicesManager servicesManager) {
        return new RegisteredServiceSimpleFormController(servicesManager, registeredServiceFactory());
    }

    private String getDefaultServiceUrl() {
        return casProperties.getMgmt().getServerName().concat(serverProperties.getContextPath()).concat("/callback");
    }

    @Bean
    public List serviceFactoryList() {
        return new ArrayList();
    }

    @Bean
    public Map<String, UniqueTicketIdGenerator> uniqueIdGeneratorsMap() {
        return new HashMap<>();
    }

    @Bean
    public List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators() {
        return new ArrayList<>();
    }

    /**
     * The Cas management security interceptor.
     */
    public class CasManagementSecurityInterceptor extends SecurityInterceptor {

        public CasManagementSecurityInterceptor() {
            super(config(), "CasClient", "securityHeaders,csrfToken,RequireAnyRoleAuthorizer");
            final DefaultSecurityLogic logic = new DefaultSecurityLogic() {
                @Override
                protected HttpAction forbidden(final WebContext context, final List currentClients, final List list, final String authorizers) {
                    return HttpAction.redirect("Authorization failed", context, "authorizationFailure");
                }

                @Override
                protected boolean loadProfilesFromSession(final WebContext context, final List currentClients) {
                    return true;
                }
            };

            logic.setSaveProfileInSession(true);
            setSecurityLogic(logic);
        }

        @Override
        public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                               final Object handler, final ModelAndView modelAndView) throws Exception {
            if (!StringUtils.isEmpty(request.getQueryString())
                    && request.getQueryString().contains(CasProtocolConstants.PARAMETER_TICKET)) {
                final RedirectView v = new RedirectView(request.getRequestURL().toString());
                v.setExposeModelAttributes(false);
                v.setExposePathVariables(false);
                modelAndView.setView(v);
            }
        }
    }
}
