package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link DynamoDbTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbTicketRegistryProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 699497009058965681L;
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public EncryptionRandomizedSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionRandomizedSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
}
