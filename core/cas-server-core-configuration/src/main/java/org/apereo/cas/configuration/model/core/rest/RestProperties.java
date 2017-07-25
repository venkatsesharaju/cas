package org.apereo.cas.configuration.model.core.rest;

/**
 * This is {@link RestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestProperties {
    /**
     * Authorization attribute name required by the REST endpoint in order to allow for the requested operation.
     * Attribute must be resolveable by the authenticated principal, or must have been already.
     */
    private String attributeName;
    /**
     * Matching authorization attribute value, pulled from the attribute
     * required by the REST endpoint in order to allow for the requested operation.
     */
    private String attributeValue;

    /**
     * The bean id of the throttler component whose job is to control rest authentication requests
     * an throttle requests per define policy.
     */
    private String throttler = "neverThrottle";
    
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(final String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getThrottler() {
        return throttler;
    }

    public void setThrottler(final String throttler) {
        this.throttler = throttler;
    }
}



