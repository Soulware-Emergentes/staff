package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tells a login an application sent the browser to from one opened by hand. The authorization
 * endpoint sends browsers to {@code /login?continue}, and the form keeps the marker through a failed
 * attempt. Only a login carrying it is named after the application whose request the session saved
 * and continues that request once the person is in.
 *
 * <p>Once the person is in, a continued login passes through {@code /continuing?continue}, which says
 * where the browser is going before it goes there.
 *
 * <p>A login opened without it ignores the saved request, which may be left over from a login
 * abandoned earlier in the same browser, discards it, and lands on the hub.
 */
final class ContinuedLogin {

    static final String PARAMETER = "continue";
    static final String LOGIN_PAGE = "/login";
    static final String CONTINUED_LOGIN_PAGE = LOGIN_PAGE + "?" + PARAMETER;
    static final String CONTINUING_PAGE = "/continuing";

    private static final String HUB_PAGE = "/";
    private static final String FAILED_LOGIN_PAGE = LOGIN_PAGE + "?error";
    private static final String FAILED_CONTINUED_LOGIN_PAGE = FAILED_LOGIN_PAGE + "&" + PARAMETER;

    private ContinuedLogin() {
    }

    /** @return whether the request carries the marker, with or without a value */
    static boolean isContinued(HttpServletRequest request) {
        return request.getParameterMap().containsKey(PARAMETER);
    }

    /**
     * Sends a continued login with a saved request on through the continuing page, and opens the hub
     * for any other, discarding whatever request the session saved.
     */
    static AuthenticationSuccessHandler successHandler() {
        RequestCache requestCache = new HttpSessionRequestCache();
        RedirectStrategy redirect = new DefaultRedirectStrategy();

        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean continues = isContinued(request) && requestCache.getRequest(request, response) != null;
            if (continues) {
                redirect.sendRedirect(request, response, CONTINUING_PAGE + "?" + PARAMETER);
                return;
            }
            requestCache.removeRequest(request, response);
            redirect.sendRedirect(request, response, HUB_PAGE);
        };
    }

    /** Shows the form again with the error, keeping the marker of a continued login. */
    static AuthenticationFailureHandler failureHandler() {
        RedirectStrategy redirect = new DefaultRedirectStrategy();

        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            String target = isContinued(request) ? FAILED_CONTINUED_LOGIN_PAGE : FAILED_LOGIN_PAGE;
            redirect.sendRedirect(request, response, target);
        };
    }
}
