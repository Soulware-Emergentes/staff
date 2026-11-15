package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The logout endpoint's answer to an application that names itself instead of handing back an ID
 * token, which Spring Authorization Server refuses on its own. A request carrying {@code id_token_hint}
 * is left to Spring, which checks the token and ends the session at once. One without it must name the
 * application by {@code client_id} and, if it asks to return, a logout URI registered for it; the
 * browser is then sent to the {@code signing-out} page, where the person confirms.
 */
final class UnhintedLogout {

    private static final String ID_TOKEN_HINT_PARAMETER = "id_token_hint";
    private static final String ERROR_URI = "https://openid.net/specs/openid-connect-rpinitiated-1_0.html#ValidationAndErrorHandling";

    private UnhintedLogout() {
    }

    /** Reads a logout request without {@code id_token_hint}, leaving any other to Spring's converter. */
    static AuthenticationConverter converter() {
        return request -> {
            if (request.getParameter(ID_TOKEN_HINT_PARAMETER) != null) {
                return null;
            }
            return new UnhintedLogoutRequest(
                SecurityContextHolder.getContext().getAuthentication(),
                single(request, PostLogoutRedirect.CLIENT_ID_PARAMETER, true),
                single(request, PostLogoutRedirect.REDIRECT_URI_PARAMETER, false),
                single(request, PostLogoutRedirect.STATE_PARAMETER, false)
            );
        };
    }

    /** Checks that the application is registered and may return where it asks to. */
    static AuthenticationProvider provider(PostLogoutRedirect redirects) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                UnhintedLogoutRequest logout = (UnhintedLogoutRequest) authentication;
                RegisteredClient client = redirects.clientOf(logout.getClientId())
                    .orElseThrow(() -> invalid(PostLogoutRedirect.CLIENT_ID_PARAMETER));

                String redirectUri = logout.getPostLogoutRedirectUri();
                if (redirectUri != null && !PostLogoutRedirect.allows(client, redirectUri)) {
                    throw invalid(PostLogoutRedirect.REDIRECT_URI_PARAMETER);
                }
                return logout.checked();
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UnhintedLogoutRequest.class.isAssignableFrom(authentication);
            }
        };
    }

    /** Sends a logout without {@code id_token_hint} to be confirmed, and leaves any other to Spring. */
    static AuthenticationSuccessHandler responseHandler() {
        AuthenticationSuccessHandler hinted = new OidcLogoutAuthenticationSuccessHandler();
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (!(authentication instanceof UnhintedLogoutRequest logout)) {
                hinted.onAuthenticationSuccess(request, response, authentication);
                return;
            }
            redirectStrategy.sendRedirect(request, response, confirmationOf(logout));
        };
    }

    private static String confirmationOf(UnhintedLogoutRequest logout) {
        UriComponentsBuilder confirmation = UriComponentsBuilder.fromPath(SigningOutInterceptor.SIGNING_OUT_PAGE)
            .queryParam(PostLogoutRedirect.CLIENT_ID_PARAMETER, logout.getClientId());
        if (logout.getPostLogoutRedirectUri() != null) {
            confirmation.queryParam(PostLogoutRedirect.REDIRECT_URI_PARAMETER, logout.getPostLogoutRedirectUri());
        }
        if (logout.getState() != null) {
            confirmation.queryParam(PostLogoutRedirect.STATE_PARAMETER, logout.getState());
        }
        return confirmation.encode().build().toUriString();
    }

    private static String single(HttpServletRequest request, String name, boolean required) {
        String[] values = request.getParameterValues(name);
        if (values == null) {
            if (required) {
                throw invalid(name);
            }
            return null;
        }
        if (values.length != 1 || values[0].isBlank()) {
            throw invalid(name);
        }
        return values[0];
    }

    private static OAuth2AuthenticationException invalid(String parameter) {
        return new OAuth2AuthenticationException(new OAuth2Error(
            OAuth2ErrorCodes.INVALID_REQUEST,
            "OpenID Connect 1.0 Logout Request Parameter: " + parameter,
            ERROR_URI
        ));
    }
}
