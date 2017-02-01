package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.core.ticket.registry.TicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.factory.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.factory.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.DefaultTicketRegistrySupport;
import org.apereo.cas.ticket.registry.NoOpLockingStrategy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.cipher.ProtocolTicketCipherExecutor;
import org.apereo.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

/**
 * This is {@link CasCoreTicketsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@AutoConfigureAfter(value = {CasCoreUtilConfiguration.class, CasCoreTicketIdGeneratorsConfiguration.class})
public class CasCoreTicketsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreTicketsConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Lazy
    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private Map<String, UniqueTicketIdGenerator> uniqueIdGeneratorsMap;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient httpClient;

    @ConditionalOnMissingBean(name = "defaultProxyGrantingTicketFactory")
    @Bean
    public ProxyGrantingTicketFactory defaultProxyGrantingTicketFactory() {
        return new DefaultProxyGrantingTicketFactory(
                ticketGrantingTicketUniqueIdGenerator(),
                grantingTicketExpirationPolicy(),
                protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultProxyTicketFactory")
    @RefreshScope
    @Bean
    @Lazy
    public ProxyTicketFactory defaultProxyTicketFactory() {
        final boolean onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultProxyTicketFactory(proxyTicketExpirationPolicy(),
                uniqueIdGeneratorsMap,
                protocolTicketCipherExecutor(),
                onlyTrackMostRecentSession);
    }

    @ConditionalOnMissingBean(name = "ticketGrantingTicketUniqueIdGenerator")
    @Bean
    public UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.TicketGrantingTicketIdGenerator(
                casProperties.getTicket().getTgt().getMaxLength(),
                casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "proxy20TicketUniqueIdGenerator")
    @Bean
    public UniqueTicketIdGenerator proxy20TicketUniqueIdGenerator() {
        return new HostNameBasedUniqueTicketIdGenerator.ProxyTicketIdGenerator(
                casProperties.getTicket().getPgt().getMaxLength(),
                casProperties.getHost().getName());
    }

    @ConditionalOnMissingBean(name = "defaultServiceTicketFactory")
    @Bean
    @Lazy
    public ServiceTicketFactory defaultServiceTicketFactory() {
        final boolean onlyTrackMostRecentSession = casProperties.getTicket().getTgt().isOnlyTrackMostRecentSession();
        return new DefaultServiceTicketFactory(serviceTicketExpirationPolicy(),
                uniqueIdGeneratorsMap,
                onlyTrackMostRecentSession,
                protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultTicketGrantingTicketFactory")
    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DefaultTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator(),
                grantingTicketExpirationPolicy(),
                protocolTicketCipherExecutor());
    }

    @ConditionalOnMissingBean(name = "defaultTicketFactory")
    @Bean
    public TicketFactory defaultTicketFactory() {
        return new DefaultTicketFactory(defaultProxyGrantingTicketFactory(),
                defaultTicketGrantingTicketFactory(),
                defaultServiceTicketFactory(),
                defaultProxyTicketFactory());
    }

    @ConditionalOnMissingBean(name = "proxy10Handler")
    @Bean
    public ProxyHandler proxy10Handler() {
        return new Cas10ProxyHandler();
    }

    @ConditionalOnMissingBean(name = "proxy20Handler")
    @Bean
    public ProxyHandler proxy20Handler() {
        return new Cas20ProxyHandler(httpClient, proxy20TicketUniqueIdGenerator());
    }

    @ConditionalOnMissingBean(name = "ticketRegistry")
    @RefreshScope
    @Bean(name = {"defaultTicketRegistry", "ticketRegistry"})
    public TicketRegistry defaultTicketRegistry() {
        final TicketRegistryProperties.InMemory mem = casProperties.getTicket().getRegistry().getInMemory();
        return new DefaultTicketRegistry(
                mem.getInitialCapacity(),
                mem.getLoadFactor(),
                mem.getConcurrency(),
                Beans.newTicketRegistryCipherExecutor(mem.getCrypto()));
    }

    @ConditionalOnMissingBean(name = "defaultTicketRegistrySupport")
    @Bean
    public TicketRegistrySupport defaultTicketRegistrySupport() {
        return new DefaultTicketRegistrySupport(ticketRegistry);
    }

    @ConditionalOnMissingBean(name = "grantingTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy grantingTicketExpirationPolicy() {
        final TicketGrantingTicketProperties tgt = casProperties.getTicket().getTgt();
        if (tgt.getRememberMe().isEnabled()) {
            final RememberMeDelegatingExpirationPolicy p = new RememberMeDelegatingExpirationPolicy();
            p.setRememberMeExpirationPolicy(new HardTimeoutExpirationPolicy(tgt.getRememberMe().getTimeToKillInSeconds()));
            p.setSessionExpirationPolicy(buildTicketGrantingTicketExpirationPolicy());
            return p;
        }

        return buildTicketGrantingTicketExpirationPolicy();
    }

    @ConditionalOnMissingBean(name = "serviceTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy serviceTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy(
                casProperties.getTicket().getSt().getNumberOfUses(),
                casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "proxyTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy proxyTicketExpirationPolicy() {
        return new MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy(
                casProperties.getTicket().getPt().getNumberOfUses(),
                casProperties.getTicket().getPt().getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "lockingStrategy")
    @Bean
    public LockingStrategy lockingStrategy() {
        return new NoOpLockingStrategy();
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleaner")
    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        final boolean isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().isEnabled();
        return new DefaultTicketRegistryCleaner(lockingStrategy(), logoutManager, ticketRegistry, isCleanerEnabled);
    }

    @ConditionalOnMissingBean(name = "ticketTransactionManager")
    @Bean
    public PlatformTransactionManager ticketTransactionManager() {
        return new PseudoTransactionManager();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "protocolTicketCipherExecutor")
    public CipherExecutor protocolTicketCipherExecutor() {
        if (casProperties.getTicket().getSecurity().isCipherEnabled()) {
            return new ProtocolTicketCipherExecutor(
                    casProperties.getTicket().getSecurity().getEncryptionKey(),
                    casProperties.getTicket().getSecurity().getSigningKey());
        }
        LOGGER.debug("Protocol tickets generated by CAS are not signed/encrypted.");
        return NoOpCipherExecutor.getInstance();
    }

    private ExpirationPolicy buildTicketGrantingTicketExpirationPolicy() {
        final TicketGrantingTicketProperties tgt = casProperties.getTicket().getTgt();
        if (tgt.getMaxTimeToLiveInSeconds() < 0 && tgt.getTimeToKillInSeconds() < 0) {
            LOGGER.warn("Ticket-granting ticket expiration policy is set to NEVER expire tickets.");
            return new NeverExpiresExpirationPolicy();
        }

        if (tgt.getTimeout().getMaxTimeToLiveInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a timeout of [{}] seconds",
                    tgt.getTimeout().getMaxTimeToLiveInSeconds());
            return new TimeoutExpirationPolicy(tgt.getTimeout().getMaxTimeToLiveInSeconds());
        }

        if (tgt.getMaxTimeToLiveInSeconds() > 0 && tgt.getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on hard/idle timeouts of [{}]/[{}] seconds",
                    tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
            return new TicketGrantingTicketExpirationPolicy(tgt.getMaxTimeToLiveInSeconds(), tgt.getTimeToKillInSeconds());
        }

        if (tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds() > 0
                && tgt.getThrottledTimeout().getTimeToKillInSeconds() > 0) {
            final ThrottledUseAndTimeoutExpirationPolicy p = new ThrottledUseAndTimeoutExpirationPolicy();
            p.setTimeToKillInSeconds(tgt.getThrottledTimeout().getTimeToKillInSeconds());
            p.setTimeInBetweenUsesInSeconds(tgt.getThrottledTimeout().getTimeInBetweenUsesInSeconds());
            LOGGER.debug("Ticket-granting ticket expiration policy is based on throttled timeouts");
            return p;
        }

        if (tgt.getHardTimeout().getTimeToKillInSeconds() > 0) {
            LOGGER.debug("Ticket-granting ticket expiration policy is based on a hard timeout of [{}] seconds",
                    tgt.getHardTimeout().getTimeToKillInSeconds());
            return new HardTimeoutExpirationPolicy(tgt.getHardTimeout().getTimeToKillInSeconds());
        }
        LOGGER.warn("Ticket-granting ticket expiration policy is set to ALWAYS expire tickets.");
        return new AlwaysExpiresExpirationPolicy();
    }
}
