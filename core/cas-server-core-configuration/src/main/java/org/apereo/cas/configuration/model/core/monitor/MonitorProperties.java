package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for cas.monitor.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class MonitorProperties {

    /**
     * The free memory threshold for the memory monitor.
     * If the amount of free memory available reaches this point
     * the memory monitor will report back a warning status as a health check.
     */
    private int freeMemThreshold = 10;

    /**
     * Options for monitoring the status a nd production of TGTs.
     */
    private Tgt tgt = new Tgt();

    /**
     * Options for monitoring the status a nd production of STs.
     */
    private St st = new St();

    /**
     * Warning options that generally deal with cache-based resources, etc.
     */
    private Warn warn = new Warn();

    /**
     * Options for monitoring sensitive CAS endpoints and resources.
     * Acts as a parent class for all endpoints and settings
     * and exposes shortcuts so security and capability of endpoints
     * can be globally controlled from one spot and then overridden elsewhere.
     */
    private Endpoints endpoints = new Endpoints();

    /**
     * Options for monitoring JDBC resources.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Options for monitoring LDAP resources.
     */
    private Ldap ldap = new Ldap();
    
    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(final Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public Warn getWarn() {
        return warn;
    }

    public void setWarn(final Warn warn) {
        this.warn = warn;
    }

    public int getFreeMemThreshold() {
        return freeMemThreshold;
    }

    public void setFreeMemThreshold(final int freeMemThreshold) {
        this.freeMemThreshold = freeMemThreshold;
    }

    public Tgt getTgt() {
        return tgt;
    }

    public void setTgt(final Tgt tgt) {
        this.tgt = tgt;
    }

    public St getSt() {
        return st;
    }

    public void setSt(final St st) {
        this.st = st;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class St {
        @NestedConfigurationProperty
        private Warn warn = new Warn(5000);

        public Warn getWarn() {
            return warn;
        }

        public void setWarn(final Warn warn) {
            this.warn = warn;
        }
    }

    public static class Tgt {

        /**
         * Warning options for monitoring TGT production.
         */
        @NestedConfigurationProperty
        private Warn warn = new Warn(10000);

        public Warn getWarn() {
            return warn;
        }

        public void setWarn(final Warn warn) {
            this.warn = warn;
        }
    }

    public static class Warn {

        /**
         * The monitor threshold where if reached, CAS might generate a warning status for health checks.
         */
        private int threshold = 10;

        /**
         * The monitor eviction threshold where if reached, CAS might generate a warning status for health checks.
         * The underlying data source and monitor (i.e. cache) must support the concept of evictions.
         */
        private long evictionThreshold;


        public Warn() {
        }

        public Warn(final int threshold) {
            this.threshold = threshold;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(final int threshold) {
            this.threshold = threshold;
        }

        public long getEvictionThreshold() {
            return evictionThreshold;
        }

        public void setEvictionThreshold(final long evictionThreshold) {
            this.evictionThreshold = evictionThreshold;
        }
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = 4722929378440179113L;

        /**
         * When monitoring the LDAP connection pool, indicates the amount of time the operation must wait
         * before it times outs and considers the pool in bad shape.
         */
        private String maxWait = "PT5S";

        /**
         * Options that define the LDAP connection pool to monitor.
         */
        @NestedConfigurationProperty
        private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

        public ConnectionPoolingProperties getPool() {
            return pool;
        }

        public void setPool(final ConnectionPoolingProperties pool) {
            this.pool = pool;
        }

        public long getMaxWait() {
            return Beans.newDuration(maxWait).toMillis();
        }

        public void setMaxWait(final String maxWait) {
            this.maxWait = maxWait;
        }
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = -7139788158851782673L;

        /**
         * The query to execute against the database to monitor status.
         */
        private String validationQuery = "SELECT 1";

        /**
         * When monitoring the JDBC connection pool, indicates the amount of time the operation must wait
         * before it times outs and considers the pool in bad shape.
         */
        private String maxWait = "PT5S";

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(final String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public long getMaxWait() {
            return Beans.newDuration(maxWait).toMillis();
        }

        public void setMaxWait(final String maxWait) {
            this.maxWait = maxWait;
        }

    }

    /**
     * All endpoints are modeled after
     * Spring Boot’s own actuator endpoints and by default are considered sensitive.
     * By default, no endpoint is enabled or allowed access.
     * Endpoints may go through multiple levels and layers of security.
     */
    public abstract static class BaseEndpoint {

        /**
         * Disable access to the endpoint completely. 
         */
        private Boolean enabled;

        /**
         * Marking the endpoint as sensitive will force it to require authentication.
         * The authentication scheme usually is done via the presence of spring security
         * related modules who then handle the protocol and verifications of credentials.
         * If you wish to choose alternative methods for endpoint security, such as letting
         * CAS handle the sensitivity of the endpoint itself via CAS itself or via
         * IP pattern checking, etc, set this flag to false. For more elaborate means of authenticating
         * into an endpoint such as basic authn and verifications credentials with a master account, LDAP, JDBC, etc
         * set this endpoint to true and configure spring security appropriate as is described by the docs.
         *
         * By default all endpoints are considered disabled and sensitive.
         *
         * <p>It's important to note that these endpoints and their settings only affect
         * what CAS provides. Additional endpoints provided by Spring Boot are controlled
         * elsewhere by Spring Boot itself.</p>
         */
        private Boolean sensitive;

        public Boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final Boolean enabled) {
            this.enabled = enabled;
        }

        public Boolean isSensitive() {
            return sensitive;
        }

        public void setSensitive(final Boolean sensitive) {
            this.sensitive = sensitive;
        }
    }


    public static class Endpoints extends BaseEndpoint {

        /**
         * Dashboard related settings.
         */
        private Dashboard dashboard = new Dashboard();

        /**
         * Audit events related settings.
         */
        private AuditEvents auditEvents = new AuditEvents();

        /**
         * Authentication events related settings.
         */
        private AuthenticationEvents authenticationEvents = new AuthenticationEvents();

        /**
         * Configuration State related settings.
         */
        private ConfigurationState configurationState = new ConfigurationState();

        /**
         * Health check related settings.
         */
        private HealthCheck healthCheck = new HealthCheck();

        /**
         * Logging configuration related settings.
         */
        private LoggingConfig loggingConfig = new LoggingConfig();

        /**
         * Metrics related settings.
         */
        private Metrics metrics = new Metrics();

        /**
         * Attribute resolution related settings.
         */
        private AttributeResolution attributeResolution = new AttributeResolution();

        /**
         * Single Sign on sessions report related settings.
         */
        private SingleSignOnReport singleSignOnReport = new SingleSignOnReport();

        /**
         * Statistics related settings.
         */
        private Statistics statistics = new Statistics();

        /**
         * Trusted devices related settings.
         */
        private TrustedDevices trustedDevices = new TrustedDevices();

        /**
         * Status related settings.
         */
        private Status status = new Status();
        /**
         * Single Sign On Status related settings.
         */
        private SingleSignOnStatus singleSignOnStatus = new SingleSignOnStatus();

        /**
         * Spring webflow related settings.
         */
        private SpringWebflowReport springWebflowReport = new SpringWebflowReport();

        /**
         * Registered services and service registry related settings.
         */
        private RegisteredServicesReport registeredServicesReport = new RegisteredServicesReport();


        public Endpoints() {
            setSensitive(Boolean.TRUE);
            setEnabled(Boolean.FALSE);
        }

        public RegisteredServicesReport getRegisteredServicesReport() {
            return registeredServicesReport;
        }

        public void setRegisteredServicesReport(final RegisteredServicesReport registeredServicesReport) {
            this.registeredServicesReport = registeredServicesReport;
        }

        public SpringWebflowReport getSpringWebflowReport() {
            return springWebflowReport;
        }

        public void setSpringWebflowReport(final SpringWebflowReport springWebflowReport) {
            this.springWebflowReport = springWebflowReport;
        }

        public SingleSignOnStatus getSingleSignOnStatus() {
            return singleSignOnStatus;
        }

        public void setSingleSignOnStatus(final SingleSignOnStatus singleSignOnStatus) {
            this.singleSignOnStatus = singleSignOnStatus;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }

        public Dashboard getDashboard() {
            return dashboard;
        }

        public void setDashboard(final Dashboard dashboard) {
            this.dashboard = dashboard;
        }

        public AuditEvents getAuditEvents() {
            return auditEvents;
        }

        public void setAuditEvents(final AuditEvents auditEvents) {
            this.auditEvents = auditEvents;
        }

        public AuthenticationEvents getAuthenticationEvents() {
            return authenticationEvents;
        }

        public void setAuthenticationEvents(final AuthenticationEvents authenticationEvents) {
            this.authenticationEvents = authenticationEvents;
        }

        public ConfigurationState getConfigurationState() {
            return configurationState;
        }

        public void setConfigurationState(final ConfigurationState configurationState) {
            this.configurationState = configurationState;
        }

        public HealthCheck getHealthCheck() {
            return healthCheck;
        }

        public void setHealthCheck(final HealthCheck healthCheck) {
            this.healthCheck = healthCheck;
        }

        public LoggingConfig getLoggingConfig() {
            return loggingConfig;
        }

        public void setLoggingConfig(final LoggingConfig loggingConfig) {
            this.loggingConfig = loggingConfig;
        }

        public Metrics getMetrics() {
            return metrics;
        }

        public void setMetrics(final Metrics metrics) {
            this.metrics = metrics;
        }

        public AttributeResolution getAttributeResolution() {
            return attributeResolution;
        }

        public void setAttributeResolution(final AttributeResolution attributeResolution) {
            this.attributeResolution = attributeResolution;
        }

        public SingleSignOnReport getSingleSignOnReport() {
            return singleSignOnReport;
        }

        public void setSingleSignOnReport(final SingleSignOnReport singleSignOnReport) {
            this.singleSignOnReport = singleSignOnReport;
        }

        public Statistics getStatistics() {
            return statistics;
        }

        public void setStatistics(final Statistics statistics) {
            this.statistics = statistics;
        }

        public TrustedDevices getTrustedDevices() {
            return trustedDevices;
        }

        public void setTrustedDevices(final TrustedDevices trustedDevices) {
            this.trustedDevices = trustedDevices;
        }

        public static class Dashboard extends BaseEndpoint {
        }

        public static class AuditEvents extends BaseEndpoint {
        }

        public static class AuthenticationEvents extends BaseEndpoint {
        }

        public static class ConfigurationState extends BaseEndpoint {
        }

        public static class HealthCheck extends BaseEndpoint {
        }

        public static class LoggingConfig extends BaseEndpoint {
        }

        public static class Metrics extends BaseEndpoint {
        }

        public static class AttributeResolution extends BaseEndpoint {
        }

        public static class SingleSignOnReport extends BaseEndpoint {
        }

        public static class Statistics extends BaseEndpoint {
        }

        public static class TrustedDevices extends BaseEndpoint {
        }

        public static class Status extends BaseEndpoint {
        }

        public static class SingleSignOnStatus extends BaseEndpoint {
        }

        public static class SpringWebflowReport extends BaseEndpoint {
        }

        public static class RegisteredServicesReport extends BaseEndpoint {
        }
    }
}
