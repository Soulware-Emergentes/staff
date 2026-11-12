package lat.soulware.staff.identityprovider.application.queries.clients.criteria;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * One registered application, named by its public client id.
 *
 * @param clientId the client id, as sent in an authorization or token request
 */
public record ClientByClientId(String clientId) implements QueryCriteria {
}
