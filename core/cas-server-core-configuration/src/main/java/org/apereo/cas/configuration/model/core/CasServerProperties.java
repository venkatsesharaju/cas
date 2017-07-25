package org.apereo.cas.configuration.model.core;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasServerProperties {

    /**
     * Location of a rewrite valve specifically by Apache Tomcat
     * to activate URL rewriting.
     */
    private Resource rewriteValveConfigLocation = new ClassPathResource("container/tomcat/rewrite.config");

    /**
     * Full name of the CAS server. This is public-facing address
     * of the CAS deployment and not the individual node address,
     * in the event that CAS is clustered.
     */
    private String name = "https://cas.example.org:8443";

    /**
     * A concatenation of the server name plus the CAS context path.
     * Deployments at root likely need to blank out this value.
     */
    private String prefix = name.concat("/cas");

    private Ajp ajp = new Ajp();

    private Http http = new Http();

    /**
     * Http proxy configuration properties.
     */
    private HttpProxy httpProxy = new HttpProxy();

    private SslValve sslValve = new SslValve();

    /**
     * Configuration properties for access logging beyond defaults.
     */
    private ExtendedAccessLog extAccessLog = new ExtendedAccessLog();

    public HttpProxy getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(final HttpProxy httpProxy) {
        this.httpProxy = httpProxy;
    }

    public Resource getRewriteValveConfigLocation() {
        return rewriteValveConfigLocation;
    }

    public void setRewriteValveConfigLocation(final Resource rewriteValveConfigLocation) {
        this.rewriteValveConfigLocation = rewriteValveConfigLocation;
    }

    public ExtendedAccessLog getExtAccessLog() {
        return extAccessLog;
    }

    public void setExtAccessLog(final ExtendedAccessLog extAccessLog) {
        this.extAccessLog = extAccessLog;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(final Http http) {
        this.http = http;
    }

    public Ajp getAjp() {
        return ajp;
    }

    public void setAjp(final Ajp ajp) {
        this.ajp = ajp;
    }

    public SslValve getSslValve() {
        return sslValve;
    }

    public void setSslValve(final SslValve sslValve) {
        this.sslValve = sslValve;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getLoginUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    public String getLogoutUrl() {
        return getPrefix().concat(CasProtocolConstants.ENDPOINT_LOGOUT);
    }

    public static class Ajp {

        /**
         * Sets the protocol to handle incoming traffic.
         */
        private String protocol = "AJP/1.3";

        /**
         * The TCP port number on which this Connector will create a server socket and await incoming connections.
         * Your operating system will allow only one server application to listen to a
         * particular port number on a particular IP address. If the special value of 0 (zero) is used,
         * then Tomcat will select a free port at random to use for this connector.
         * This is typically only useful in embedded and testing applications.
         */
        private int port = 8009;

        /**
         * Set this attribute to true if you wish to have calls to request.isSecure() to return true for requests received
         * by this Connector (you would want this on an SSL Connector). The default value is false.
         */
        private boolean secure;

        /**
         * A boolean value which can be used to enable or disable
         * the TRACE HTTP method. If not specified, this attribute is set to false.
         */
        private boolean allowTrace;

        /**
         * Set this attribute to the name of the protocol you wish to have returned by calls to request.getScheme(). For example,
         * you would set this attribute to "https" for an SSL Connector.
         */
        private String scheme = "http";

        /**
         * Enable AJP support in CAS for the embedded Apache Tomcat container.
         */
        private boolean enabled;
        private String asyncTimeout = "PT5S";
        private boolean enableLookups;
        /**
         * The maximum size in bytes of the POST which will be handled by the container
         * FORM URL parameter parsing. The feature can be disabled by setting this attribute to a value
         * less than or equal to 0. If not specified, this attribute is set to 2097152 (2 megabytes).
         */
        private int maxPostSize = 20971520;
        /**
         * If this Connector is being used in a proxy configuration, configure this attribute
         * to specify the server port to be returned for calls to request.getServerPort().
         */
        private int proxyPort = -1;
        /**
         * If this Connector is supporting non-SSL requests, and a request is received
         * for which a matching &lt;security-constraint&gt; requires SSL transport,
         * Catalina will automatically redirect the request to the port number specified here.
         */
        private int redirectPort = -1;
        /**
         * Additional attributes to be set on the AJP connector in form of key-value pairs.
         * Examples include:
         * <p>
         * <ul>
         * <li><code>tomcatAuthentication</code>: If set to true, the authentication will be done in Tomcat.
         * Otherwise, the authenticated principal will be propagated from the native webserver
         * and used for authorization in Tomcat.
         * Note that this principal will have no roles associated with it. The default value is true.</li>
         * <li><code>maxThreads</code>: The maximum number of request processing threads to be created
         * by this Connector, which therefore determines the maximum number of simultaneous
         * requests that can be handled. If not specified, this attribute is set to 200.
         * If an executor is associated with this connector, this attribute is
         * ignored as the connector will execute tasks using the executor rather than an internal thread pool.</li>
         * <li><code>keepAliveTimeout</code>: The number of milliseconds this Connector
         * will wait for another AJP request before closing the connection.
         * The default value is to use the value that has been set for the connectionTimeout attribute.</li>
         * <li><code>maxCookieCount</code>: The maximum number of cookies that are permitted for a request.
         * A value of less than zero means no limit. If not specified, a default value of 200 will be used.</li>
         * <li><code>bufferSize</code>: The size of the output buffer to use. If less than or equal to zero,
         * then output buffering is disabled. The default value is -1 (i.e. buffering disabled)</li>
         * <li><code>clientCertProvider</code>: When client certificate information is presented in a
         * form other than instances of java.security.cert.X509Certificate it needs to be converted
         * before it can be used and this property controls which JSSE provider is used to perform
         * the conversion. For example it is used with the AJP connectors,
         * the HTTP APR connector and with the org.apache.catalina.valves.SSLValve.If not specified,
         * the default provider will be used.</li>
         * <li><code>connectionTimeout</code>: The number of milliseconds this Connector
         * will wait, after accepting a connection,
         * for the request URI line to be presented. The default value is infinite (i.e. no timeout).</li>
         * <li><code>address</code>: For servers with more than one IP address,
         * this attribute specifies which address will be used for listening on
         * the specified port. By default, this port will be used on all IP addresses associated with the server.
         * A value of 127.0.0.1 indicates that the Connector will only listen on the loopback interface.</li>
         * </ul>
         *
         * See the Apache Tomcat documentation for a full list.
         */
        private Map<String, Object> attributes = new LinkedHashMap<>();

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(final boolean secure) {
            this.secure = secure;
        }

        public boolean isAllowTrace() {
            return allowTrace;
        }

        public void setAllowTrace(final boolean allowTrace) {
            this.allowTrace = allowTrace;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(final String scheme) {
            this.scheme = scheme;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getAsyncTimeout() {
            return Beans.newDuration(asyncTimeout).toMillis();
        }

        public void setAsyncTimeout(final String asyncTimeout) {
            this.asyncTimeout = asyncTimeout;
        }

        public boolean isEnableLookups() {
            return enableLookups;
        }

        public void setEnableLookups(final boolean enableLookups) {
            this.enableLookups = enableLookups;
        }

        public int getMaxPostSize() {
            return maxPostSize;
        }

        public void setMaxPostSize(final int maxPostSize) {
            this.maxPostSize = maxPostSize;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(final int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public int getRedirectPort() {
            return redirectPort;
        }

        public void setRedirectPort(final int redirectPort) {
            this.redirectPort = redirectPort;
        }
    }

    public static class ExtendedAccessLog {

        /**
         * Flag to indicate whether extended log facility is enabled.
         */
        private boolean enabled;

        /**
         * String representing extended log pattern.
         */
        private String pattern = "c-ip s-ip cs-uri sc-status time X-threadname x-H(secure) x-H(remoteUser)";

        /**
         * File name suffix for extended log.
         */
        private String suffix = ".log";

        /**
         * File name prefix for extended log.
         */
        private String prefix = "localhost_access_extended";

        /**
         * Directory name for extended log.
         */
        private String directory;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(final String directory) {
            this.directory = directory;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(final String pattern) {
            this.pattern = pattern;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(final String suffix) {
            this.suffix = suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }

    public static class HttpProxy {
        private boolean enabled;
        private String scheme = "https";
        private boolean secure = true;
        private int redirectPort;
        private int proxyPort;
        private String protocol = "AJP/1.3";
        private Map<String, Object> attributes = new LinkedHashMap<>();

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }

        public int getRedirectPort() {
            return redirectPort;
        }

        public void setRedirectPort(final int redirectPort) {
            this.redirectPort = redirectPort;
        }

        public int getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(final int proxyPort) {
            this.proxyPort = proxyPort;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(final String scheme) {
            this.scheme = scheme;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(final boolean secure) {
            this.secure = secure;
        }
    }

    public static class Http {

        /**
         * Enable a separate port for the embedded container for HTTP access.
         */
        private boolean enabled;

        /**
         * The HTTP port to use.
         */
        private int port = 8080;

        /**
         * HTTP protocol to use.
         */
        private String protocol = "org.apache.coyote.http11.Http11NioProtocol";

        /**
         * Additional attributes to be set on the connector.
         */
        private Map<String, Object> attributes = new LinkedHashMap<>();

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(final Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(final int port) {
            this.port = port;
        }
    }

    public static class SslValve {
        /**
         * Enable the SSL valve for apache tomcat.
         */
        private boolean enabled;

        /**
         * Allows setting a custom name for the ssl_client_cert header.
         * If not specified, the default of ssl_client_cert is used.
         */
        private String sslClientCertHeader = "ssl_client_cert";
        /**
         * Allows setting a custom name for the ssl_cipher header.
         * If not specified, the default of ssl_cipher is used.
         */
        private String sslCipherHeader = "ssl_cipher";
        /**
         * Allows setting a custom name for the ssl_session_id header.
         * If not specified, the default of ssl_session_id is used.
         */
        private String sslSessionIdHeader = "ssl_session_id";
        /**
         * Allows setting a custom name for the ssl_cipher_usekeysize header.
         * If not specified, the default of ssl_cipher_usekeysize is used.
         */
        private String sslCipherUserKeySizeHeader = "ssl_cipher_usekeysize";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getSslClientCertHeader() {
            return sslClientCertHeader;
        }

        public void setSslClientCertHeader(final String sslClientCertHeader) {
            this.sslClientCertHeader = sslClientCertHeader;
        }

        public String getSslCipherHeader() {
            return sslCipherHeader;
        }

        public void setSslCipherHeader(final String sslCipherHeader) {
            this.sslCipherHeader = sslCipherHeader;
        }

        public String getSslSessionIdHeader() {
            return sslSessionIdHeader;
        }

        public void setSslSessionIdHeader(final String sslSessionIdHeader) {
            this.sslSessionIdHeader = sslSessionIdHeader;
        }

        public String getSslCipherUserKeySizeHeader() {
            return sslCipherUserKeySizeHeader;
        }

        public void setSslCipherUserKeySizeHeader(final String sslCipherUserKeySizeHeader) {
            this.sslCipherUserKeySizeHeader = sslCipherUserKeySizeHeader;
        }
    }
}
