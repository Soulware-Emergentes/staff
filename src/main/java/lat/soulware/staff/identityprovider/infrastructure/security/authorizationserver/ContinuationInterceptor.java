package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gives the page shown between a continued login and the application it continues to the address
 * it goes on to: the authorization request Spring Security saved, which ends at the application's
 * registered redirect URI. With no saved request there is nowhere to continue to, and the browser
 * is sent to the hub instead.
 */
@Component
public class ContinuationInterceptor implements HandlerInterceptor {

    static final String TARGET_ATTRIBUTE = "continueTo";

    private static final String HUB_PAGE = "/";

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (this.requestCache.getRequest(request, response) != null) {
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
        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
        if (modelAndView == null || savedRequest == null) {
            return;
        }
        modelAndView.addObject(TARGET_ATTRIBUTE, savedRequest.getRedirectUrl());
    }
}
