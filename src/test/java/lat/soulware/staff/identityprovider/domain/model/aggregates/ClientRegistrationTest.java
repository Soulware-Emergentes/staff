package lat.soulware.staff.identityprovider.domain.model.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import lat.soulware.staff.identityprovider.domain.model.exceptions.LoginWithoutHomeUriException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.LoginWithoutRedirectUriException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingClientGrantTypesException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.PublicClientWithConfidentialGrantException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.RefreshWithoutLoginException;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientGrantType;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientId;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientKind;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientRegistrationId;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientSecretHash;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.HomeUri;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.RedirectUri;

/** Whether a client can keep a secret and how it gets tokens are separate, within the limits a missing secret sets. */
class ClientRegistrationTest {

    private static final ClientSecretHash SECRET = new ClientSecretHash("$2a$10$hash");
    private static final HomeUri HOME = new HomeUri("http://localhost:4200/");
    private static final Set<RedirectUri> CALLBACK = Set.of(new RedirectUri("http://localhost:4200/bff/callback"));

    @Test
    void aConfidentialClientLogsPeopleInAndRenewsTheirTokens() {
        ClientRegistration bff = register(
            ClientKind.CONFIDENTIAL, SECRET,
            Set.of(ClientGrantType.AUTHORIZATION_CODE, ClientGrantType.REFRESH_TOKEN), HOME, CALLBACK
        );

        assertThat(bff.getGrantTypes())
            .containsExactlyInAnyOrder(ClientGrantType.AUTHORIZATION_CODE, ClientGrantType.REFRESH_TOKEN);
    }

    @Test
    void aConfidentialClientGetsTokensForItselfWithoutLoggingAnyoneIn() {
        ClientRegistration service = register(
            ClientKind.CONFIDENTIAL, SECRET, Set.of(ClientGrantType.CLIENT_CREDENTIALS), null, Set.of()
        );

        assertThat(service.getHomeUri()).isNull();
    }

    @Test
    void aPublicClientLogsPeopleIn() {
        ClientRegistration frontend = register(
            ClientKind.PUBLIC, null, Set.of(ClientGrantType.AUTHORIZATION_CODE), HOME, CALLBACK
        );

        assertThat(frontend.getGrantTypes()).containsExactly(ClientGrantType.AUTHORIZATION_CODE);
    }

    @Test
    void aPublicClientCannotRenewTokens() {
        assertThatThrownBy(() -> register(
            ClientKind.PUBLIC, null,
            Set.of(ClientGrantType.AUTHORIZATION_CODE, ClientGrantType.REFRESH_TOKEN), HOME, CALLBACK
        )).isInstanceOf(PublicClientWithConfidentialGrantException.class);
    }

    @Test
    void aPublicClientCannotGetTokensForItself() {
        assertThatThrownBy(() -> register(
            ClientKind.PUBLIC, null, Set.of(ClientGrantType.CLIENT_CREDENTIALS), null, Set.of()
        )).isInstanceOf(PublicClientWithConfidentialGrantException.class);
    }

    @Test
    void aClientWithNoGrantIsRefused() {
        assertThatThrownBy(() -> register(ClientKind.CONFIDENTIAL, SECRET, Set.of(), null, Set.of()))
            .isInstanceOf(MissingClientGrantTypesException.class);
    }

    @Test
    void renewingWithoutLoggingInIsRefused() {
        assertThatThrownBy(() -> register(
            ClientKind.CONFIDENTIAL, SECRET,
            Set.of(ClientGrantType.CLIENT_CREDENTIALS, ClientGrantType.REFRESH_TOKEN), null, Set.of()
        )).isInstanceOf(RefreshWithoutLoginException.class);
    }

    @Test
    void loggingInWithoutACallbackIsRefused() {
        assertThatThrownBy(() -> register(
            ClientKind.CONFIDENTIAL, SECRET, Set.of(ClientGrantType.AUTHORIZATION_CODE), HOME, Set.of()
        )).isInstanceOf(LoginWithoutRedirectUriException.class);
    }

    @Test
    void loggingInWithoutAHomeIsRefused() {
        assertThatThrownBy(() -> register(
            ClientKind.CONFIDENTIAL, SECRET, Set.of(ClientGrantType.AUTHORIZATION_CODE), null, CALLBACK
        )).isInstanceOf(LoginWithoutHomeUriException.class);
    }

    private static ClientRegistration register(
        ClientKind kind,
        ClientSecretHash secret,
        Set<ClientGrantType> grantTypes,
        HomeUri homeUri,
        Set<RedirectUri> loginRedirectUris
    ) {
        return ClientRegistration.reconstitute(
            new ClientRegistrationId(UUID.randomUUID()),
            new ClientId("client"),
            kind,
            secret,
            grantTypes,
            homeUri,
            loginRedirectUris,
            Set.of(),
            Set.of()
        );
    }
}
