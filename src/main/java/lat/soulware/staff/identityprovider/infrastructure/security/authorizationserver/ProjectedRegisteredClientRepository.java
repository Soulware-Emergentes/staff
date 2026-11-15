package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientById;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;

/**
 * The registered applications, as the authorization server reads them. A client's kind decides how
 * it authenticates: a confidential one with its secret, a public one with nothing but PKCE. Its grant
 * types decide what it may do: log people in, with its APIs and a role to act as on them; renew their
 * tokens; or get tokens for itself, with its APIs and the scopes granted to it.
 *
 * <p>Every login uses PKCE, the client's secret notwithstanding, as OAuth 2.1 asks. A renewable login
 * lasts {@code staff.oidc.session.max} from the moment the person logged in: the refresh token is
 * reused rather than rotated, which only a client able to keep it secret is allowed.
 *
 * <p>Registrations are entered as data, so saving through here is refused.
 */
@Component
public class ProjectedRegisteredClientRepository implements RegisteredClientRepository {

    private static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String CLIENT_CREDENTIALS = "CLIENT_CREDENTIALS";

    private final ClientProjection clientProjection;
    private final Duration sessionMax;

    public ProjectedRegisteredClientRepository(
        ClientProjection clientProjection,
        @Value("${staff.oidc.session.max}") Duration sessionMax
    ) {
        this.clientProjection = clientProjection;
        this.sessionMax = sessionMax;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("Client registrations are entered as data");
    }

    @Override
    public RegisteredClient findById(String id) {
        return this.clientProjection.findById(new ClientById(UUID.fromString(id)))
            .map(this::toRegisteredClient)
            .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return this.clientProjection.findByClientId(new ClientByClientId(clientId))
            .map(this::toRegisteredClient)
            .orElse(null);
    }

    private RegisteredClient toRegisteredClient(ClientResult client) {
        boolean logsPeopleIn = client.grantTypes().contains(AUTHORIZATION_CODE);
        boolean renews = client.grantTypes().contains(REFRESH_TOKEN);

        RegisteredClient.Builder builder = RegisteredClient.withId(client.id().toString())
            .clientId(client.clientId())
            .clientSettings(ClientSettings.builder()
                .requireProofKey(logsPeopleIn)
                .requireAuthorizationConsent(false)
                .setting(RequestedAccess.RESOURCES_SETTING, client.resources())
                .build()
            );
        client.resources().forEach(builder::scope);

        if ("CONFIDENTIAL".equals(client.kind())) {
            builder.clientSecret(client.secretHash())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
        } else {
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
        }

        if (logsPeopleIn) {
            builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE);
            client.loginRedirectUris().forEach(builder::redirectUri);
            client.logoutRedirectUris().forEach(builder::postLogoutRedirectUri);
            client.roles().forEach(role -> builder.scope(RequestedAccess.ACT_AS_PREFIX + role));
        }
        if (renews) {
            builder.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .tokenSettings(TokenSettings.builder()
                    .refreshTokenTimeToLive(this.sessionMax)
                    .reuseRefreshTokens(true)
                    .build()
                );
        }
        if (client.grantTypes().contains(CLIENT_CREDENTIALS)) {
            builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
            client.grantedScopes().forEach(builder::scope);
        }
        return builder.build();
    }
}
