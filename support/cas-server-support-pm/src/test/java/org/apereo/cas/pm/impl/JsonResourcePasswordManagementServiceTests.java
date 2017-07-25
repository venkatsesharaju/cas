package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JsonResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreUtilConfiguration.class,
        PasswordManagementConfiguration.class})
@TestPropertySource(locations = {"classpath:/pm.properties"})
public class JsonResourcePasswordManagementServiceTests {

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Test
    public void verifyUserEmailCanBeFound() {
        final String email = passwordChangeService.findEmail("casuser");
        assertEquals(email, "casuser@example.org");
    }

    @Test
    public void verifyUserEmailCanNotBeFound() {
        final String email = passwordChangeService.findEmail("casusernotfound");
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        final Map questions = passwordChangeService.getSecurityQuestions("casuser");
        assertEquals(questions.size(), 2);
    }

    @Test
    public void verifyUserPasswordChange() {
        final Credential c = new UsernamePasswordCredential("casuser", "password");
        final PasswordChangeBean bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        final boolean res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }
}
