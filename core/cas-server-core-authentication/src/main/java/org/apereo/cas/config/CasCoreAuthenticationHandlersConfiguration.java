package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ProxyingPrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasCoreAuthenticationHandlersConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationHandlersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationHandlersConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient supportsTrustStoreSslSocketFactoryHttpClient;

    @Autowired(required = false)
    @Qualifier("acceptPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration acceptPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("jaasPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration jaasPasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "jaasPrincipalFactory")
    @Bean
    public PrincipalFactory jaasPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jaasAuthenticationHandler() {
        final JaasAuthenticationHandler h = new JaasAuthenticationHandler();

        h.setKerberosKdcSystemProperty(casProperties.getAuthn().getJaas().getKerberosKdcSystemProperty());
        h.setKerberosRealmSystemProperty(casProperties.getAuthn().getJaas().getKerberosRealmSystemProperty());
        h.setRealm(casProperties.getAuthn().getJaas().getRealm());
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getJaas().getPasswordEncoder()));

        if (jaasPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(jaasPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getJaas().getPrincipalTransformation()));

        h.setPrincipalFactory(jaasPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setName(casProperties.getAuthn().getJaas().getName());
        return h;
    }

    @Bean
    public AuthenticationHandler proxyAuthenticationHandler() {
        final HttpBasedServiceCredentialsAuthenticationHandler h = new HttpBasedServiceCredentialsAuthenticationHandler();
        h.setHttpClient(supportsTrustStoreSslSocketFactoryHttpClient);
        h.setPrincipalFactory(proxyPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setOrder(Integer.MIN_VALUE);
        return h;
    }

    @ConditionalOnMissingBean(name = "proxyPrincipalFactory")
    @Bean
    public PrincipalFactory proxyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        final ProxyingPrincipalResolver p = new ProxyingPrincipalResolver();
        p.setPrincipalFactory(proxyPrincipalFactory());
        return p;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        final AcceptUsersAuthenticationHandler h = new AcceptUsersAuthenticationHandler();
        h.setUsers(getParsedUsers());
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getAccept().getPasswordEncoder()));
        if (acceptPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(acceptPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getAccept().getPrincipalTransformation()));
        h.setPrincipalFactory(acceptUsersPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setName(casProperties.getAuthn().getAccept().getName());
        return h;
    }

    @ConditionalOnMissingBean(name = "acceptUsersPrincipalFactory")
    @Bean
    public PrincipalFactory acceptUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    private Map<String, String> getParsedUsers() {
        final Pattern pattern = Pattern.compile("::");

        final String usersProperty = casProperties.getAuthn().getAccept().getUsers();

        if (StringUtils.isNotBlank(usersProperty) && usersProperty.contains(pattern.pattern())) {
            return Stream.of(usersProperty.split(","))
                    .map(pattern::split)
                    .collect(Collectors.toMap(userAndPassword -> userAndPassword[0], userAndPassword -> userAndPassword[1]));
        }
        return Collections.emptyMap();
    }

    /**
     * The type Proxy authentication event execution plan configuration.
     */
    @Configuration("proxyAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class ProxyAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(proxyAuthenticationHandler(), proxyPrincipalResolver());
        }
    }

    /**
     * The type Jaas authentication event execution plan configuration.
     */
    @Configuration("jaasAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class JaasAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Autowired
        @Qualifier("personDirectoryPrincipalResolver")
        private PrincipalResolver personDirectoryPrincipalResolver;
        
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getJaas().getRealm())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(jaasAuthenticationHandler(), personDirectoryPrincipalResolver);
            }
        }
    }
}
