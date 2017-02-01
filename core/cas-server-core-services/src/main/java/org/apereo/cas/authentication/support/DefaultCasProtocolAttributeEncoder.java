package org.apereo.cas.authentication.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The default implementation of the attribute
 * encoder that will use a per-service key-pair
 * to encrypt the credential password and PGT
 * when available. All other attributes remain in
 * place.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultCasProtocolAttributeEncoder extends AbstractProtocolAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasProtocolAttributeEncoder.class);
    
    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager the services manager
     */
    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    /**
     * Instantiates a new Default cas attribute encoder.
     *
     * @param servicesManager the services manager
     * @param cipherExecutor  the cipher executor
     */
    public DefaultCasProtocolAttributeEncoder(final ServicesManager servicesManager, final RegisteredServiceCipherExecutor cipherExecutor) {
        super(servicesManager, cipherExecutor);
    }

    /**
     * Encode and encrypt credential password using the public key
     * supplied by the service. The result is base64 encoded
     * and put into the attributes collection again, overwriting
     * the previous value.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encodeAndEncryptCredentialPassword(final Map<String, Object> attributes,
                                                      final Map<String, String> cachedAttributesToEncode,
                                                      final RegisteredServiceCipherExecutor cipher,
                                                      final RegisteredService registeredService) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL_CREDENTIAL,
                cipher, registeredService);
    }

    /**
     * Encode and encrypt pgt.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encodeAndEncryptProxyGrantingTicket(final Map<String, Object> attributes,
                                                       final Map<String, String> cachedAttributesToEncode,
                                                       final RegisteredServiceCipherExecutor cipher,
                                                       final RegisteredService registeredService) {
        encryptAndEncodeAndPutIntoAttributesMap(attributes, cachedAttributesToEncode,
                CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, cipher, registeredService);
    }

    /**
     * Encrypt, encode and put the attribute into attributes map.
     *
     * @param attributes               the attributes
     * @param cachedAttributesToEncode the cached attributes to encode
     * @param cachedAttributeName      the cached attribute name
     * @param cipher                   the cipher
     * @param registeredService        the registered service
     */
    protected void encryptAndEncodeAndPutIntoAttributesMap(final Map<String, Object> attributes,
                                                           final Map<String, String> cachedAttributesToEncode,
                                                           final String cachedAttributeName,
                                                           final RegisteredServiceCipherExecutor cipher,
                                                           final RegisteredService registeredService) {
        final String cachedAttribute = cachedAttributesToEncode.remove(cachedAttributeName);
        if (StringUtils.isNotBlank(cachedAttribute)) {
            LOGGER.debug("Retrieved [{}] as a cached model attribute...", cachedAttributeName);
            final String encodedValue = cipher.encode(cachedAttribute, registeredService);
            if (StringUtils.isNotBlank(encodedValue)) {
                attributes.put(cachedAttributeName, encodedValue);
                LOGGER.debug("Encrypted and encoded [{}] as an attribute to [{}].", cachedAttributeName, encodedValue);
            }
        } else {
            LOGGER.debug("[{}] is not available as a cached model attribute to encrypt...", cachedAttributeName);
        }
    }

    @Override
    protected void encodeAttributesInternal(final Map<String, Object> attributes,
                                            final Map<String, String> cachedAttributesToEncode,
                                            final RegisteredServiceCipherExecutor cipher,
                                            final RegisteredService registeredService) {
        encodeAndEncryptCredentialPassword(attributes, cachedAttributesToEncode, cipher, registeredService);
        encodeAndEncryptProxyGrantingTicket(attributes, cachedAttributesToEncode, cipher, registeredService);
        sanitizeAndTransformAttributeNames(attributes, registeredService);
    }

    private void sanitizeAndTransformAttributeNames(final Map<String, Object> attributes,
                                                    final RegisteredService registeredService) {
        LOGGER.debug("Sanitizing attribute names in preparation of the final validation response");

        final Set<Pair<String, Object>> attrs = attributes.keySet().stream()
                .filter(s -> s.contains(":"))
                .map(s -> Pair.of(s.replace(':', '_'), attributes.get(s)))
                .collect(Collectors.toSet());
        if (!attrs.isEmpty()) {
            LOGGER.debug("Found [{}] attribute(s) that need to be sanitized/encoded.", attrs);
            attributes.entrySet().removeIf(s -> s.getKey().contains(":"));
            attrs.forEach(p -> {
                LOGGER.debug("Sanitized attribute name to be [{}]", p.getKey());
                attributes.put(p.getKey(), p.getValue());
            });
        }
    }
}
