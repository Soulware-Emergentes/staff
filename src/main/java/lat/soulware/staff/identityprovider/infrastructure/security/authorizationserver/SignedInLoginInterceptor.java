package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Keeps a browser that has already logged in off the login form. A continued login with a saved
 * request goes on through the continuing page to the application that asked for it; any other goes
 * to the hub.
 */
@Component
public class SignedInLoginInterceptor implements HandlerInterceptor {

    private static final String HUB_PAGE = "/";
    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        boolean hasLoggedIn = TRUST_RESOLVER.isAuthenticated(SecurityContextHolder.getContext().getAuthentication());
        if (!hasLoggedIn) {
            return true;
        }
        boolean continues = ContinuedLogin.isContinued(request) && this.requestCache.getRequest(request, response) != null;
        String target = continues ? ContinuedLogin.CONTINUING_PAGE + "?" + ContinuedLogin.PARAMETER : HUB_PAGE;
        response.sendRedirect(request.getContextPath() + target);
        return false;
    }
}
