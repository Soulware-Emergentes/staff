package lat.soulware.staff.identityprovider.domain.model.aggregates;

import java.util.Set;

import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingResourceIdentifierException;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ApiResourceId;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.AppRole;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ResourceIdentifier;
import lat.soulware.staff.identityprovider.domain.model.valueobjects.ServiceScope;
import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.aggregates.PackagedAs;

/**
 * An API registered with the identity provider, one per deployment: {@code sgt-api-dev} and
 * {@code sgt-api-prod} are two resources. It defines the roles people may hold in it and the scopes
 * services may be granted on it; what any of them means is the API's own business.
 */
@PackagedAs("apiresource")
public class ApiResource extends AggregateRoot<ApiResourceId> {
    private final ApiResourceId id;
    private final ResourceIdentifier identifier;
    private final Set<AppRole> roles;
    private final Set<ServiceScope> scopes;

    private ApiResource(ApiResourceId id, ResourceIdentifier identifier, Set<AppRole> roles, Set<ServiceScope> scopes) {
        if (identifier == null) {
            throw new MissingResourceIdentifierException();
        }
        this.id = id;
        this.identifier = identifier;
        this.roles = Set.copyOf(roles);
        this.scopes = Set.copyOf(scopes);
    }

    /**
     * Rebuilds an API resource as registered.
     *
     * @param id         the resource's identity
     * @param identifier the name tokens for it carry as their audience
     * @param roles      the roles it defines for people
     * @param scopes     the scopes it defines for services
     * @return the resource as stored
     */
    public static ApiResource reconstitute(
        ApiResourceId id,
        ResourceIdentifier identifier,
        Set<AppRole> roles,
        Set<ServiceScope> scopes
    ) {
        return new ApiResource(id, identifier, roles, scopes);
    }

    @Override
    public ApiResourceId getId() {
        return this.id;
    }

    public ResourceIdentifier getIdentifier() {
        return this.identifier;
    }

    public Set<AppRole> getRoles() {
        return this.roles;
    }

    public Set<ServiceScope> getScopes() {
        return this.scopes;
    }
}
