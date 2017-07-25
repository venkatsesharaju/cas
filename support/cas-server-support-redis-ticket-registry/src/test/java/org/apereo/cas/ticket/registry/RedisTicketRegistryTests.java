package org.apereo.cas.ticket.registry;

import java.util.Arrays;
import java.util.Collection;

import org.apereo.cas.config.RedisTicketRegistryConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {RedisTicketRegistryConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations={"classpath:/redis.properties"})
public class RedisTicketRegistryTests extends AbstractTicketRegistryTests {

    private static RedisServer REDIS_SERVER;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public RedisTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        return Arrays.asList(false, true);
    }

    @BeforeClass
    public static void startRedis() throws Exception {
        REDIS_SERVER = new RedisServer(6379);
        REDIS_SERVER.start();
    }

    @AfterClass
    public static void stopRedis() {
        REDIS_SERVER.stop();
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return this.ticketRegistry;
    }
}
