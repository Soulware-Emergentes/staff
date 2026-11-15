package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Where a sign-out returns to. An application asking for one names itself by {@code client_id} and
 * where to come back by {@code post_logout_redirect_uri}, which must be one of the logout URIs
 * registered for it, so a link cannot send the browser anywhere else; its {@code state} comes back
 * with it. Anything short of that ends on the login form, telling the person they signed out.
 */
@Component
public class PostLogoutRedirect implements LogoutSuccessHandler {

    static final String CLIENT_ID_PARAMETER = "client_id";
    static final String REDIRECT_URI_PARAMETER = "post_logout_redirect_uri";
    static final String STATE_PARAMETER = "state";

    private static final String SIGNED_OUT_PAGE = ContinuedLogin.LOGIN_PAGE + "?logout";

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final RegisteredClientRepository clients;

    public PostLogoutRedirect(RegisteredClientRepository clients) {
        this.clients = clients;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
        this.redirectStrategy.sendRedirect(request, response, this.targetOf(request).orElse(SIGNED_OUT_PAGE));
    }

    /** @return the registered application named by {@code client_id} */
    Optional<RegisteredClient> clientOf(String clientId) {
        return Optional.ofNullable(clientId).map(this.clients::findByClientId);
    }

    /** @return whether the application registered {@code redirectUri} as where a logout may return to */
    static boolean allows(RegisteredClient client, String redirectUri) {
        return client.getPostLogoutRedirectUris().contains(redirectUri);
    }

    /** @return where the request asks to return to, with its state, when the application registered it */
    Optional<String> targetOf(HttpServletRequest request) {
        String redirectUri = request.getParameter(REDIRECT_URI_PARAMETER);
        if (!StringUtils.hasText(redirectUri)) {
            return Optional.empty();
        }

        return this.clientOf(request.getParameter(CLIENT_ID_PARAMETER))
            .filter(client -> allows(client, redirectUri))
            .map(client -> withState(redirectUri, request.getParameter(STATE_PARAMETER)));
    }

    private static String withState(String redirectUri, String state) {
        UriComponentsBuilder target = UriComponentsBuilder.fromUriString(redirectUri);
        if (StringUtils.hasText(state)) {
            target.queryParam(STATE_PARAMETER, UriUtils.encode(state, StandardCharsets.UTF_8));
        }
        return target.build(true).toUriString();
    }
}
