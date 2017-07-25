package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link IgniteTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("igniteTicketRegistryTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class IgniteTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteTicketRegistryTicketCatalogConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    private void setTicketGrantingTicketProperties(final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("ticketGrantingTicketsCache");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
    }

    private void setServiceTicketDefinitionProperties(final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("serviceTicketsCache");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
    }
}
