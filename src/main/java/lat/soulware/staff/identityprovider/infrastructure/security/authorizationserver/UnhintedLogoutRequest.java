package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * A logout an application asks for by naming itself rather than handing back an ID token, as OpenID
 * Connect RP-Initiated Logout allows. With nothing to prove which person the application means, staff
 * asks the person before ending their session.
 */
final class UnhintedLogoutRequest extends AbstractAuthenticationToken {

    private final Authentication principal;
    private final String clientId;
    private final String postLogoutRedirectUri;
    private final String state;

    UnhintedLogoutRequest(Authentication principal, String clientId, String postLogoutRedirectUri, String state) {
        this(principal, clientId, postLogoutRedirectUri, state, false);
    }

    private UnhintedLogoutRequest(
        Authentication principal,
        String clientId,
        String postLogoutRedirectUri,
        String state,
        boolean checked
    ) {
        super(List.of());
        this.principal = principal;
        this.clientId = clientId;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
        this.state = state;
        super.setAuthenticated(checked);
    }

    /** @return the same request, once the application and where it returns to were checked */
    UnhintedLogoutRequest checked() {
        return new UnhintedLogoutRequest(this.principal, this.clientId, this.postLogoutRedirectUri, this.state, true);
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    String getClientId() {
        return this.clientId;
    }

    /** @return where the application asks to return to, null when it asks for nothing */
    String getPostLogoutRedirectUri() {
        return this.postLogoutRedirectUri;
    }

    /** @return the value the application asks to get back, null when it asks for nothing */
    String getState() {
        return this.state;
    }
}
