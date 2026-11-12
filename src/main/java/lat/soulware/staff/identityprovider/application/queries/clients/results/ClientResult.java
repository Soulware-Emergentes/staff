package lat.soulware.staff.identityprovider.application.queries.clients.results;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * A registered application, with everything the authorization server checks its requests against.
 *
 * @param id                 the registration's identity
 * @param clientId           the application's public name
 * @param kind               {@code PUBLIC} or {@code CONFIDENTIAL}
 * @param secretHash         the BCrypt hash of its secret, null for a public client
 * @param loginRedirectUris  where a login may return to
 * @param logoutRedirectUris where a logout may return to
 * @param resources          the identifiers of the APIs it may get tokens for
 * @param roles              every role those APIs define, which a login may ask to act as
 * @param grantedScopes      the scopes granted to it on those APIs, for tokens it gets for itself
 */
public record ClientResult(
    UUID id,
    String clientId,
    String kind,
    String secretHash,
    List<String> loginRedirectUris,
    List<String> logoutRedirectUris,
    List<String> resources,
    List<String> roles,
    List<String> grantedScopes
) implements QueryResult {

    public ClientResult {
        loginRedirectUris = List.copyOf(loginRedirectUris);
        logoutRedirectUris = List.copyOf(logoutRedirectUris);
        resources = List.copyOf(resources);
        roles = List.copyOf(roles);
        grantedScopes = List.copyOf(grantedScopes);
    }
}
