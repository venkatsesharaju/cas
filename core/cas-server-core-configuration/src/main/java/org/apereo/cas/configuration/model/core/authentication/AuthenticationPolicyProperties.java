package org.apereo.cas.configuration.model.core.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for cas.authn.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class AuthenticationPolicyProperties {

    /**
     * Global authentication policy that is applied when CAS attempts to vend and validate tickets.
     * Checks to make sure a particular authentication handler has successfully executed and validated credentials.
     * Required handlers are defined per registered service.
     */
    private boolean requiredHandlerAuthenticationPolicyEnabled;

    /**
     * Satisfied if any authentication handler succeeds.
     * Allows options to avoid short circuiting and try every handler even if one prior succeeded.
     */
    private Any any = new Any();

    /**
     * Satisfied if an only if a specified handler successfully authenticates its credential.
     */
    private Req req = new Req();

    /**
     * Satisfied if and only if all given credentials are successfully authenticated.
     * Support for multiple credentials is new in CAS and this handler would
     * only be acceptable in a multi-factor authentication situation.
     */
    private All all = new All();

    /**
     * Execute a groovy script to detect authentication policy.
     */
    private List<Groovy> groovy = new ArrayList<>();

    /**
     * Execute a rest endpoint to detect authentication policy.
     */
    private List<Rest> rest = new ArrayList<>();

    /**
     * Satisfied if an only if the authentication event is not blocked by a <code>PreventedException</code>.
     */
    private NotPrevented notPrevented = new NotPrevented();
    
    public All getAll() {
        return all;
    }

    public void setAll(final All all) {
        this.all = all;
    }

    public NotPrevented getNotPrevented() {
        return notPrevented;
    }

    public void setNotPrevented(final NotPrevented notPrevented) {
        this.notPrevented = notPrevented;
    }

    public Any getAny() {
        return any;
    }

    public void setAny(final Any any) {
        this.any = any;
    }

    public Req getReq() {
        return req;
    }

    public void setReq(final Req req) {
        this.req = req;
    }

    public boolean isRequiredHandlerAuthenticationPolicyEnabled() {
        return requiredHandlerAuthenticationPolicyEnabled;
    }

    public void setRequiredHandlerAuthenticationPolicyEnabled(final boolean v) {
        this.requiredHandlerAuthenticationPolicyEnabled = v;
    }
    
    public static class NotPrevented {
        /**
         * Enables the policy.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Any {
        /**
         * Avoid short circuiting and try every handler even if one prior succeeded.
         * Ensure number of provided credentials does not match the sum of authentication successes and failures
         */
        private boolean tryAll;

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }
    }

    public static class All {

        /**
         * Enables the policy.
         */
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Req {
        /**
         * Enables the policy.
         */
        private boolean enabled;

        /**
         * Ensure number of provided credentials does not match the sum of authentication successes and failures.
         */
        private boolean tryAll;

        /**
         * The handler name which must have successfully executed and validated credentials.
         */
        private String handlerName = "handlerName";

        public boolean isTryAll() {
            return tryAll;
        }

        public void setTryAll(final boolean tryAll) {
            this.tryAll = tryAll;
        }

        public String getHandlerName() {
            return handlerName;
        }

        public void setHandlerName(final String handlerName) {
            this.handlerName = handlerName;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }
    }

    public List<Groovy> getGroovy() {
        return groovy;
    }

    public void setGroovy(final List<Groovy> groovy) {
        this.groovy = groovy;
    }

    public List<Rest> getRest() {
        return rest;
    }

    public void setRest(final List<Rest> rest) {
        this.rest = rest;
    }

    public static class Rest {
        /**
         * Rest endpoint url to contact.
         */
        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public static class Groovy {
        /**
         * Path to the groovy script to execute.
         */
        private String script;

        public String getScript() {
            return script;
        }

        public void setScript(final String script) {
            this.script = script;
        }
    }
}
