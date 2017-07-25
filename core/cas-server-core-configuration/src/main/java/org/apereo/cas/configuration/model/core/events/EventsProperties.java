package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoClientProperties;

/**
 * Configuration properties class for events.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class EventsProperties {

    /**
     * Whether geolocation should be tracked as part of collected authentication events.
     * This of course require's consent from the user's browser to collect stats on location.
     */
    private boolean trackGeolocation;

    /**
     * Whether CAS should track the underlying configuration store for changes.
     * This depends on whether the store provides that sort of functionality.
     * When running in standalone mode, this typically translates to CAS monitoring
     * configuration files and reloading context conditionally if there are any changes.
     */
    private boolean trackConfigurationModifications = true;

    /**
     * Track authentication events inside a database.
     */
    private Jpa jpa = new Jpa();

    /**
     * Track authentication events inside a mongodb instance.
     */
    private Mongodb mongodb = new Mongodb();

    public Mongodb getMongodb() {
        return mongodb;
    }

    public void setMongodb(final Mongodb mongodb) {
        this.mongodb = mongodb;
    }

    public boolean isTrackGeolocation() {
        return trackGeolocation;
    }

    public void setTrackGeolocation(final boolean trackGeolocation) {
        this.trackGeolocation = trackGeolocation;
    }

    public boolean isTrackConfigurationModifications() {
        return trackConfigurationModifications;
    }

    public void setTrackConfigurationModifications(final boolean trackConfigurationModifications) {
        this.trackConfigurationModifications = trackConfigurationModifications;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 7647381223153797806L;
    }

    public static class Mongodb extends AbstractMongoClientProperties {
        private static final long serialVersionUID = -1918436901491275547L;

        public Mongodb() {
            setCollection("MongoDbCasEventRepository");
        }
    }
}
