package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link ChainingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ChainingAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {
    private static final long serialVersionUID = 3795054936775326709L;

    private List<RegisteredServiceAttributeReleasePolicy> policies = new ArrayList<>();

    public List<RegisteredServiceAttributeReleasePolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(final List<RegisteredServiceAttributeReleasePolicy> policies) {
        this.policies = policies;
    }

    @Override
    public Map<String, Object> getAttributes(final Principal p, final RegisteredService service) {
        final Map<String, Object> attributes = new HashMap<>();
        policies.forEach(policy -> attributes.putAll(policy.getAttributes(p, service)));
        return attributes;
    }
}
