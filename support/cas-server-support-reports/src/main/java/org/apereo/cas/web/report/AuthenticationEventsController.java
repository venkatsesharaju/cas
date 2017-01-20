package org.apereo.cas.web.report;

import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link AuthenticationEventsController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("authenticationEventsController")
@RequestMapping("/status/authnEvents")
@ConditionalOnClass(value = CasEventRepository.class)
public class AuthenticationEventsController {

    private CasEventRepository eventRepository;

    public AuthenticationEventsController(final CasEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView("monitoring/viewAuthenticationEvents");
    }

    /**
     * Gets records.
     *
     * @param request  the request
     * @param response the response
     * @return the records
     * @throws Exception the exception
     */
    @GetMapping(value = "/getEvents")
    @ResponseBody
    public Collection<CasEvent> getRecords(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return this.eventRepository.load();
    }
}
