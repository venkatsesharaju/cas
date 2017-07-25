package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoClientProperties;
import org.apereo.cas.configuration.support.AbstractConfigProperties;

import java.io.Serializable;

/**
 * This is {@link GAuthMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GAuthMultifactorProperties extends BaseMultifactorProvider {
    private static final long serialVersionUID = -7401748853833491119L;
    /**
     * Issuer used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    private String issuer = "CASIssuer";
    /**
     * Label used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    private String label = "CASLabel";
    /**
     * Length of the generated code.
     */
    private int codeDigits = 6;
    /**
     * The expiration time of the generated code in seconds.
     */
    private long timeStepSize = 30;
    /**
     * Since TOTP passwords are time-based, it is essential that the clock of both the server and
     * the client are synchronised within
     * the tolerance defined here as the window size.
     */
    private int windowSize = 3;

    private MongoDb mongodb = new MongoDb();
    private Jpa jpa = new Jpa();
    private Json json = new Json();
    private Rest rest = new Rest();

    private Cleaner cleaner = new Cleaner();

    public GAuthMultifactorProperties() {
        setId("mfa-gauth");
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public MongoDb getMongodb() {
        return mongodb;
    }

    public void setMongodb(final MongoDb mongodb) {
        this.mongodb = mongodb;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public int getCodeDigits() {
        return codeDigits;
    }

    public void setCodeDigits(final int codeDigits) {
        this.codeDigits = codeDigits;
    }

    public long getTimeStepSize() {
        return timeStepSize;
    }

    public void setTimeStepSize(final long timeStepSize) {
        this.timeStepSize = timeStepSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(final int windowSize) {
        this.windowSize = windowSize;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public static class Json extends AbstractConfigProperties {
        private static final long serialVersionUID = 4303355159388663888L;
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = 4518622579150572559L;
        /**
         * Endpoint url of the REST resource used for tokens that are kept to prevent replay attacks.
         */
        private String endpointUrl;

        public String getEndpointUrl() {
            return endpointUrl;
        }

        public void setEndpointUrl(final String endpointUrl) {
            this.endpointUrl = endpointUrl;
        }
    }

    public static class MongoDb extends AbstractMongoClientProperties {
        private static final long serialVersionUID = -200556119517414696L;
        /**
         * Collection name where tokens are kept to prevent replay attacks.
         */
        private String tokenCollection;

        public MongoDb() {
            setCollection("MongoDbGoogleAuthenticatorRepository");
            setTokenCollection("MongoDbGoogleAuthenticatorTokenRepository");
        }

        public String getTokenCollection() {
            return tokenCollection;
        }

        public void setTokenCollection(final String tokenCollection) {
            this.tokenCollection = tokenCollection;
        }
    }

    public static class Jpa implements Serializable {
        private static final long serialVersionUID = -2689797889546802618L;
        /**
         * Database instance where tokens are kept to prevent replay attacks.
         */
        private Database database = new Database();

        public Database getDatabase() {
            return database;
        }

        public void setDatabase(final Database database) {
            this.database = database;
        }

        public static class Database extends AbstractJpaProperties {
            private static final long serialVersionUID = -7446381055350251885L;

            public Database() {
                super.setUrl("jdbc:hsqldb:mem:cas-gauth");
            }
        }
    }

    public static class Cleaner implements Serializable {
        private static final long serialVersionUID = -6036042153454544990L;
        /**
         * Enable the cleaner to run as a background process to remove expired records.
         */
        private boolean enabled = true;
        /**
         * Background job's startup delay.
         */
        private String startDelay = "PT1M";
        /**
         * Repeat interval of the background job. Defines recurrence.
         */
        private String repeatInterval = "PT1M";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(final String startDelay) {
            this.startDelay = startDelay;
        }

        public String getRepeatInterval() {
            return repeatInterval;
        }

        public void setRepeatInterval(final String repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }
}
