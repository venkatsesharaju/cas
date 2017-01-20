package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.DefaultWebApplicationResponseBuilderLocator;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.support.DefaultCasProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractResourceBasedServiceRegistryDao;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.services.ServiceRegistryInitializer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.services.DefaultRegisteredServiceCipherExecutor;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

/**
 * This is {@link CasCoreServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreServicesConfiguration {

    private static final String BEAN_NAME_SERVICE_REGISTRY_DAO = "serviceRegistryDao";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @RefreshScope
    @Bean
    public MultifactorTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {
        final String attributeNameTriggers = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers();
        final String requestParameter = casProperties.getAuthn().getMfa().getRequestParameter();

        return new DefaultMultifactorTriggerSelectionStrategy(attributeNameTriggers, requestParameter);
    }

    @RefreshScope
    @Bean
    public PersistentIdGenerator shibbolethCompatiblePersistentIdGenerator() {
        return new ShibbolethCompatiblePersistentIdGenerator();
    }
    
    @Bean
    public ResponseBuilderLocator webApplicationResponseBuilderLocator() {
        return new DefaultWebApplicationResponseBuilderLocator();
    }

    @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
    @Bean
    public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder() {
        return new WebApplicationServiceResponseBuilder();
    }

    @RefreshScope
    @Bean
    public ProtocolAttributeEncoder casAttributeEncoder(@Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao) {
        return new DefaultCasProtocolAttributeEncoder(servicesManager(serviceRegistryDao), registeredServiceCipherExecutor());
    }

    @Bean
    public ProtocolAttributeEncoder noOpCasAttributeEncoder() {
        return new NoOpProtocolAttributeEncoder();
    }

    @Bean
    public RegisteredServiceCipherExecutor registeredServiceCipherExecutor() {
        return new DefaultRegisteredServiceCipherExecutor();
    }

    @Bean
    public ServicesManager servicesManager(@Qualifier("serviceRegistryDao") final ServiceRegistryDao serviceRegistryDao) {
        return new DefaultServicesManager(serviceRegistryDao);
    }

    @ConditionalOnMissingBean(name = BEAN_NAME_SERVICE_REGISTRY_DAO)
    @Bean(name = {BEAN_NAME_SERVICE_REGISTRY_DAO, "inMemoryServiceRegistryDao"})
    public ServiceRegistryDao inMemoryServiceRegistryDao() {
        final InMemoryServiceRegistry impl = new InMemoryServiceRegistry();
        if (context.containsBean("inMemoryRegisteredServices")) {
            final List list = context.getBean("inMemoryRegisteredServices", List.class);
            impl.setRegisteredServices(list);
        }
        return impl;
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryInitializer serviceRegistryInitializer(@Qualifier(BEAN_NAME_SERVICE_REGISTRY_DAO) final ServiceRegistryDao serviceRegistryDao) {
        return new ServiceRegistryInitializer(embeddedJsonServiceRegistry(eventPublisher), serviceRegistryDao, servicesManager(serviceRegistryDao),
                casProperties.getServiceRegistry().isInitFromJson());
    }

    @Autowired
    @ConditionalOnMissingBean(name = "jsonServiceRegistryDao")
    @Bean
    public ServiceRegistryDao embeddedJsonServiceRegistry(final ApplicationEventPublisher publisher) {
        try {
            return new EmbeddedServiceRegistryDao(publisher);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * The embedded service registry that processes built-in JSON service files
     * on the classpath.
     */
    public static class EmbeddedServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {
        EmbeddedServiceRegistryDao(final ApplicationEventPublisher publisher) throws Exception {
            super(new ClassPathResource("services"), new RegisteredServiceJsonSerializer(), false, publisher);
        }

        @Override
        protected String getExtension() {
            return "json";
        }
    }
}
