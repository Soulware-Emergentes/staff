package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver.HubInterceptor.Application;

/**
 * Fills the notice a hub card passes through on its way to an application, naming it the way a
 * continued login does and going on to its home URI. The application is looked up among those the
 * person can open, by the client id the card names, so a link can neither send the browser anywhere
 * else nor open what the person holds no role on; for anything else the browser goes back to the hub.
 */
@Component
public class OpeningInterceptor implements HandlerInterceptor {

    static final String CLIENT_PARAMETER = "client";

    private static final String HUB_PAGE = "/";

    private final HubInterceptor hub;

    public OpeningInterceptor(HubInterceptor hub) {
        this.hub = hub;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (this.opened(request).isPresent()) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + HUB_PAGE);
        return false;
    }

    @Override
    public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView
    ) {
        if (modelAndView == null) {
            return;
        }
        this.opened(request).ifPresent(application -> {
            modelAndView.addObject(RequestingApplicationInterceptor.APPLICATION_ATTRIBUTE, application.name());
            modelAndView.addObject(RequestingApplicationInterceptor.ENVIRONMENT_ATTRIBUTE, application.environmentName());
            modelAndView.addObject(ContinuationInterceptor.TARGET_ATTRIBUTE, application.homeUri());
        });
    }

    private Optional<Application> opened(HttpServletRequest request) {
        Authentication person = SecurityContextHolder.getContext().getAuthentication();
        String clientId = request.getParameter(CLIENT_PARAMETER);
        if (person == null || clientId == null) {
            return Optional.empty();
        }

        return this.hub.launchableBy(UUID.fromString(person.getName()), RequestContextUtils.getLocale(request))
            .stream()
            .flatMap(environment -> environment.applications().stream())
            .filter(application -> application.clientId().equals(clientId))
            .findFirst();
    }
}
