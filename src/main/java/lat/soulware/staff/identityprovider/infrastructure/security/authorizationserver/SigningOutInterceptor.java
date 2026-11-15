package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;

/**
 * Fills the page that asks a person to confirm a sign-out an application asked for without an ID
 * token. It names the application by its registration and carries, to the sign-out form, only where
 * the application may return to, which {@link PostLogoutRedirect} checks again when the form is sent.
 * A browser that is not signed in has nothing to confirm and is sent straight back.
 */
@Component
public class SigningOutInterceptor implements HandlerInterceptor {

    static final String SIGNING_OUT_PAGE = "/signing-out";

    static final String CLIENT_ID_ATTRIBUTE = "logoutClientId";
    static final String REDIRECT_URI_ATTRIBUTE = "postLogoutRedirectUri";
    static final String STATE_ATTRIBUTE = "logoutState";

    private static final String SIGNED_OUT_PAGE = ContinuedLogin.LOGIN_PAGE + "?logout";
    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final PostLogoutRedirect redirects;
    private final ClientProjection clientProjection;
    private final RequestingApplicationInterceptor requestingApplication;

    public SigningOutInterceptor(
        PostLogoutRedirect redirects,
        ClientProjection clientProjection,
        RequestingApplicationInterceptor requestingApplication
    ) {
        this.redirects = redirects;
        this.clientProjection = clientProjection;
        this.requestingApplication = requestingApplication;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (TRUST_RESOLVER.isAuthenticated(SecurityContextHolder.getContext().getAuthentication())) {
            return true;
        }
        this.redirectStrategy.sendRedirect(request, response, this.redirects.targetOf(request).orElse(SIGNED_OUT_PAGE));
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
        String clientId = request.getParameter(PostLogoutRedirect.CLIENT_ID_PARAMETER);
        if (clientId == null) {
            return;
        }
        this.clientProjection.findByClientId(new ClientByClientId(clientId))
            .ifPresent(client -> this.requestingApplication.name(client, request, modelAndView));

        if (this.redirects.targetOf(request).isPresent()) {
            modelAndView.addObject(CLIENT_ID_ATTRIBUTE, clientId);
            modelAndView.addObject(REDIRECT_URI_ATTRIBUTE, request.getParameter(PostLogoutRedirect.REDIRECT_URI_PARAMETER));
            modelAndView.addObject(STATE_ATTRIBUTE, request.getParameter(PostLogoutRedirect.STATE_PARAMETER));
        }
    }
}
