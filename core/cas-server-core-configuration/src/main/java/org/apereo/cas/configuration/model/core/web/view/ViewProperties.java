package org.apereo.cas.configuration.model.core.web.view;

import java.io.Serializable;

/**
 * This is {@link ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ViewProperties implements Serializable {
    private static final long serialVersionUID = 2719748442042197738L;
    private String defaultRedirectUrl;

    private Cas2 cas2 = new Cas2();
    private Cas3 cas3 = new Cas3();

    public Cas2 getCas2() {
        return cas2;
    }

    public Cas3 getCas3() {
        return cas3;
    }

    public void setCas2(final Cas2 cas2) {
        this.cas2 = cas2;
    }

    public void setCas3(final Cas3 cas3) {
        this.cas3 = cas3;
    }

    public String getDefaultRedirectUrl() {
        return defaultRedirectUrl;
    }

    public void setDefaultRedirectUrl(final String defaultRedirectUrl) {
        this.defaultRedirectUrl = defaultRedirectUrl;
    }

    public static class Cas2 implements Serializable {
        private static final long serialVersionUID = -7954879759474698003L;
        private String success = "protocol/2.0/casServiceValidationSuccess";
        private String failure = "protocol/2.0/casServiceValidationFailure";
        private boolean v3ForwardCompatible;

        private Proxy proxy = new Proxy();

        public boolean isV3ForwardCompatible() {
            return v3ForwardCompatible;
        }

        public void setV3ForwardCompatible(final boolean v3ForwardCompatible) {
            this.v3ForwardCompatible = v3ForwardCompatible;
        }

        public Proxy getProxy() {
            return proxy;
        }

        public void setProxy(final Proxy proxy) {
            this.proxy = proxy;
        }

        public String getSuccess() {
            return success;
        }

        public void setSuccess(final String success) {
            this.success = success;
        }

        public String getFailure() {
            return failure;
        }

        public void setFailure(final String failure) {
            this.failure = failure;
        }

        public static class Proxy implements Serializable {
            private static final long serialVersionUID = 6765987342872282599L;
            private String success = "protocol/2.0/casProxySuccessView";
            private String failure = "protocol/2.0/casProxyFailureView";

            public String getSuccess() {
                return success;
            }

            public void setSuccess(final String success) {
                this.success = success;
            }

            public String getFailure() {
                return failure;
            }

            public void setFailure(final String failure) {
                this.failure = failure;
            }
        }
    }

    public static class Cas3 implements Serializable {
        private static final long serialVersionUID = -2345062034300650858L;
        private String success = "protocol/3.0/casServiceValidationSuccess";
        private String failure = "protocol/3.0/casServiceValidationFailure";

        public String getSuccess() {
            return success;
        }

        public void setSuccess(final String success) {
            this.success = success;
        }

        public String getFailure() {
            return failure;
        }

        public void setFailure(final String failure) {
            this.failure = failure;
        }
    }
}
