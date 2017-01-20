package org.apereo.cas.ticket.registry;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.HazelcastTicketRegistryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        HazelcastTicketRegistryConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutConfiguration.class})
@ContextConfiguration(locations="classpath:HazelcastInstanceConfigurationTests-config.xml")
@TestPropertySource(properties = {"cas.ticket.registry.hazelcast.configLocation="})
@DirtiesContext
public class DefaultHazelcastInstanceConfigurationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHazelcastInstanceConfigurationTests.class);

    @Autowired
    @Qualifier("hazelcast")
    private HazelcastInstance hzInstance;

    public HazelcastInstance getHzInstance() {
        return hzInstance;
    }

    @Test
    public void correctHazelcastInstanceIsCreated() throws Exception {
        assertNotNull(this.hzInstance);
        final Config config = this.hzInstance.getConfig();
        assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
        assertEquals(Arrays.asList("localhost"), config.getNetworkConfig().getJoin().getTcpIpConfig().getMembers());
        assertTrue(config.getNetworkConfig().isPortAutoIncrement());
        assertEquals(5701, config.getNetworkConfig().getPort());

        final MapConfig mapConfig = config.getMapConfig("tickets");
        assertNotNull(mapConfig);
        assertEquals(28800, mapConfig.getMaxIdleSeconds());
        assertEquals(EvictionPolicy.LRU, mapConfig.getEvictionPolicy());
    }

    @After
    public void shutdownHz() {
        LOGGER.info("Shutting down hazelcast instance {}", this.hzInstance.getConfig().getInstanceName());
        this.hzInstance.shutdown();
        while (this.hzInstance.getLifecycleService().isRunning()) {
            LOGGER.info("Waiting for instances to shut down");
        }
    }
}
