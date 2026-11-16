package lat.soulware.staff.identityprovider.application.queries.grants.criteria;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * The scopes one application is granted on one API, for tokens it gets for itself.
 *
 * @param clientId the application's client id
 * @param resource the API's identifier
 */
public record ScopesOfClient(String clientId, String resource) implements QueryCriteria {
}
