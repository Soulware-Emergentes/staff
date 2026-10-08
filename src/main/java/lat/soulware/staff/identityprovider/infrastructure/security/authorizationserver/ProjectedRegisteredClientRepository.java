package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.UUID;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientById;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;

/**
 * The registered applications, as the authorization server reads them. A public client logs people
 * in with PKCE and may ask for its APIs, plus a role to act as on them; a confidential one gets
 * tokens for itself with its secret, and may ask for its APIs plus the scopes granted to it.
 *
 * <p>Registrations are entered as data, so saving through here is refused.
 */
@Component
public class ProjectedRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientProjection clientProjection;

    public ProjectedRegisteredClientRepository(ClientProjection clientProjection) {
        this.clientProjection = clientProjection;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("Client registrations are entered as data");
    }

    @Override
    public RegisteredClient findById(String id) {
        return this.clientProjection.findById(new ClientById(UUID.fromString(id)))
            .map(ProjectedRegisteredClientRepository::toRegisteredClient)
            .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return this.clientProjection.findByClientId(new ClientByClientId(clientId))
            .map(ProjectedRegisteredClientRepository::toRegisteredClient)
            .orElse(null);
    }

    private static RegisteredClient toRegisteredClient(ClientResult client) {
        boolean isConfidential = "CONFIDENTIAL".equals(client.kind());
        RegisteredClient.Builder builder = RegisteredClient.withId(client.id().toString())
            .clientId(client.clientId())
            .clientSettings(ClientSettings.builder()
                .requireProofKey(!isConfidential)
                .requireAuthorizationConsent(false)
                .setting(RequestedAccess.RESOURCES_SETTING, client.resources())
                .build()
            );
        client.resources().forEach(builder::scope);

        if (isConfidential) {
            builder.clientSecret(client.secretHash())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
            client.grantedScopes().forEach(builder::scope);
            return builder.build();
        }

        builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE);
        client.loginRedirectUris().forEach(builder::redirectUri);
        client.logoutRedirectUris().forEach(builder::postLogoutRedirectUri);
        client.roles().forEach(role -> builder.scope(RequestedAccess.ACT_AS_PREFIX + role));
        return builder.build();
    }
}
