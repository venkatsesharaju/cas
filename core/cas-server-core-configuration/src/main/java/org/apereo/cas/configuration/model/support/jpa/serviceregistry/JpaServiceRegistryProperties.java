package org.apereo.cas.configuration.model.support.jpa.serviceregistry;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

/**
 * Configuration properties class for svcreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class JpaServiceRegistryProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = 352435146313504995L;

    public JpaServiceRegistryProperties() {
        super.setUrl("jdbc:hsqldb:mem:cas-service-registry");
    }
}
