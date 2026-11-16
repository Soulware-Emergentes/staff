package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * What a client asked for, read off its scopes: the API the token is for, named by its identifier,
 * and, on a login, the role to act as, named {@value #ACT_AS_PREFIX}{@code <role>}. A request names
 * at most one API, so the token's audience is never ambiguous.
 *
 * @param resources the identifiers among the scopes that name an API the client may call
 * @param actAs     the role asked for, empty when none
 */
record RequestedAccess(List<String> resources, Optional<String> actAs) {

    static final String ACT_AS_PREFIX = "act-as:";

    /** The name the client settings hold the identifiers of a client's APIs under. */
    static final String RESOURCES_SETTING = "staff.resources";

    /**
     * @param scopes          the scopes asked for or authorized
     * @param clientResources the identifiers of the APIs the client may call
     * @return what the scopes ask for
     */
    static RequestedAccess of(Set<String> scopes, Collection<String> clientResources) {
        List<String> resources = scopes.stream()
            .filter(clientResources::contains)
            .sorted()
            .toList();
        Optional<String> actAs = scopes.stream()
            .filter(scope -> scope.startsWith(ACT_AS_PREFIX))
            .map(scope -> scope.substring(ACT_AS_PREFIX.length()))
            .findFirst();

        return new RequestedAccess(resources, actAs);
    }

    /** @return the one API asked for, empty when none or more than one was */
    Optional<String> resource() {
        return this.resources.size() == 1
            ? Optional.of(this.resources.getFirst())
            : Optional.empty();
    }
}
