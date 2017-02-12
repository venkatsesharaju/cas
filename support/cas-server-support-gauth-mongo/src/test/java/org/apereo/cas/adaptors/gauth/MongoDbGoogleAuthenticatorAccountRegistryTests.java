package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GoogleAuthentiacatorMongoDbConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * This is {@link MongoDbGoogleAuthenticatorAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {GoogleAuthentiacatorMongoDbConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
                AopAutoConfiguration.class,
                CasCoreUtilConfiguration.class,
                RefreshAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(locations = {"classpath:/mongogauth.properties"})
@EnableScheduling
public class MongoDbGoogleAuthenticatorAccountRegistryTests {
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Test
    public void verifySave() {
        registry.save("uid", "secret", 143211, Arrays.asList(1, 2, 3, 4, 5, 6));
        final String s = registry.getSecret("uid");
        assertEquals(s, "secret");
    }
}
