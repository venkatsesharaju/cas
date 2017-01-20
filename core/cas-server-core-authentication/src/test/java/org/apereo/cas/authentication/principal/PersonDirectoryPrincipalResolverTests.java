package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.EchoingPrincipalResolver;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class PersonDirectoryPrincipalResolverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyNullPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        final Principal p = resolver.resolve(() -> null, CoreAuthenticationTestUtils.getPrincipal());
        assertNull(p);
    }

    @Test
    public void verifyNullAttributes() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setReturnNullIfNoAttributes(true);
        resolver.setPrincipalAttributeName(CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setPrincipalAttributeName(CoreAuthenticationTestUtils.CONST_USERNAME);
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());
        resolver.setPrincipalAttributeName("cn");
        final Credential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c, null);
        assertNotNull(p);
        assertNotEquals(p.getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

    @Test
    public void verifyChainingResolverOverwrite() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(resolver, new EchoingPrincipalResolver()));
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("cn", "changedCN");
        attributes.put("attr1", "value1");
        final Principal p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, attributes));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey("attr1"));
        assertTrue(p.getAttributes().containsKey("cn"));
        assertTrue(CollectionUtils.toCollection(p.getAttributes().get("cn")).contains("changedCN"));
    }

    @Test
    public void verifyChainingResolver() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(resolver, new EchoingPrincipalResolver()));
        final Principal p = chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME,
                        Collections.singletonMap("attr1", "value")));
        assertEquals(p.getAttributes().size(), CoreAuthenticationTestUtils.getAttributeRepository().getPossibleUserAttributeNames().size() + 1);
        assertTrue(p.getAttributes().containsKey("attr1"));
    }

    @Test
    public void verifyChainingResolverDistinct() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(CoreAuthenticationTestUtils.getAttributeRepository());

        final ChainingPrincipalResolver chain = new ChainingPrincipalResolver();
        chain.setChain(Arrays.asList(resolver, new EchoingPrincipalResolver()));

        this.thrown.expect(PrincipalException.class);
        this.thrown.expectMessage("Resolved principals by the chain are not unique");

        chain.resolve(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getPrincipal("somethingelse", Collections.singletonMap("attr1", "value")));
    }
}
