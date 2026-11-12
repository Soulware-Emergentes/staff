package lat.soulware.staff.identityprovider.domain.model.aggregates;

import java.util.Set;

import lat.soulware.staff.identityprovider.domain.model.exceptions.ConfidentialClientWithoutSecretException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingClientIdException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingClientKindException;
import lat.soulware.staff.identityprovider.domain.model.exceptions.PublicClientWithSecretException;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientId;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientKind;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientRegistrationId;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientSecretHash;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.RedirectUri;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ResourceIdentifier;
import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.aggregates.PackagedAs;

/**
 * An application registered with the identity provider, one per deployment. A public client, such as
 * a frontend, logs people in and gets tokens on their behalf; a confidential one, such as a backend,
 * gets tokens for itself with its secret. Either may only get tokens for the resources it is linked to.
 */
@PackagedAs("clientregistration")
public class ClientRegistration extends AggregateRoot<ClientRegistrationId> {
    private final ClientRegistrationId id;
    private final ClientId clientId;
    private final ClientKind kind;
    private final ClientSecretHash secretHash;
    private final Set<RedirectUri> loginRedirectUris;
    private final Set<RedirectUri> logoutRedirectUris;
    private final Set<ResourceIdentifier> resources;

    private ClientRegistration(
        ClientRegistrationId id,
        ClientId clientId,
        ClientKind kind,
        ClientSecretHash secretHash,
        Set<RedirectUri> loginRedirectUris,
        Set<RedirectUri> logoutRedirectUris,
        Set<ResourceIdentifier> resources
    ) {
        if (clientId == null) {
            throw new MissingClientIdException();
        }
        if (kind == null) {
            throw new MissingClientKindException();
        }
        boolean isConfidential = kind == ClientKind.CONFIDENTIAL;
        if (isConfidential && secretHash == null) {
            throw new ConfidentialClientWithoutSecretException();
        }
        if (!isConfidential && secretHash != null) {
            throw new PublicClientWithSecretException();
        }
        this.id = id;
        this.clientId = clientId;
        this.kind = kind;
        this.secretHash = secretHash;
        this.loginRedirectUris = Set.copyOf(loginRedirectUris);
        this.logoutRedirectUris = Set.copyOf(logoutRedirectUris);
        this.resources = Set.copyOf(resources);
    }

    /**
     * Rebuilds a registration as stored.
     *
     * @param id                 the registration's identity
     * @param clientId           the application's public name
     * @param kind               whether it can keep a secret
     * @param secretHash         its secret's hash, null for a public client
     * @param loginRedirectUris  where a login may return to
     * @param logoutRedirectUris where a logout may return to
     * @param resources          the APIs it may get tokens for
     * @return the registration as stored
     */
    public static ClientRegistration reconstitute(
        ClientRegistrationId id,
        ClientId clientId,
        ClientKind kind,
        ClientSecretHash secretHash,
        Set<RedirectUri> loginRedirectUris,
        Set<RedirectUri> logoutRedirectUris,
        Set<ResourceIdentifier> resources
    ) {
        return new ClientRegistration(
            id,
            clientId,
            kind,
            secretHash,
            loginRedirectUris,
            logoutRedirectUris,
            resources
        );
    }

    @Override
    public ClientRegistrationId getId() {
        return this.id;
    }

    public ClientId getClientId() {
        return this.clientId;
    }

    public ClientKind getKind() {
        return this.kind;
    }

    /** @return its secret's hash, null for a public client */
    public ClientSecretHash getSecretHash() {
        return this.secretHash;
    }

    public Set<RedirectUri> getLoginRedirectUris() {
        return this.loginRedirectUris;
    }

    public Set<RedirectUri> getLogoutRedirectUris() {
        return this.logoutRedirectUris;
    }

    public Set<ResourceIdentifier> getResources() {
        return this.resources;
    }
}
