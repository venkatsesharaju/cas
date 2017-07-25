package org.apereo.cas.web.flow;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.QRUtils;
import org.apereo.cas.web.support.WebUtils;
import org.jsqrl.config.SqrlConfig;
import org.jsqrl.server.JSqrlServer;
import org.jsqrl.util.SqrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

/**
 * This is {@link SqrlInitialAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlInitialAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqrlInitialAction.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private final SqrlConfig config;
    private final JSqrlServer jSqrlServer;

    public SqrlInitialAction(final SqrlConfig config, final JSqrlServer jSqrlServer) {
        this.config = config;
        this.jSqrlServer = jSqrlServer;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);

        requestContext.getFlowScope().put("sqrlEnabled", Boolean.TRUE);

        LOGGER.debug("Creating SQRL authentication request for [{}]", request.getRemoteAddr());
        final String sqrlNut = jSqrlServer.createAuthenticationRequest(request.getRemoteAddr(), true);
        LOGGER.debug("Created SQRL nut [{}]", sqrlNut);

        final String sfn = SqrlUtil.unpaddedBase64UrlEncoded(config.getSfn());
        LOGGER.debug("Encoded SQRL sfn is [{}]", sfn);

        final String prefix = casProperties.getServer().getPrefix();
        final String url = prefix.replaceAll("https?://", "qrl://")
                + "/sqrl/authn?nut=" + sqrlNut + "&sfn=" + sfn;
        LOGGER.debug("Generating SQRL QR code based on URL [{}]", url);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             Base64OutputStream os = new Base64OutputStream(out)) {
            QRUtils.generateQRCode(os, url, QRUtils.WIDTH_MEDIUM, QRUtils.WIDTH_MEDIUM);
            final String result = new String(out.toByteArray());
            LOGGER.debug("Generated SQRL QR code for [{}]", url);
            requestContext.getFlowScope().put("sqrlImage", result);
        }

        requestContext.getFlowScope().put("sqrlUrl", url);
        requestContext.getFlowScope().put("sqrlUrlEncoded", EncodingUtils.encodeBase64(url.getBytes()));
        requestContext.getFlowScope().put("nut", sqrlNut);
        requestContext.getFlowScope().put("sfn", sfn);
        requestContext.getFlowScope().put("casServerUrl", prefix);

        return null;
    }
}
