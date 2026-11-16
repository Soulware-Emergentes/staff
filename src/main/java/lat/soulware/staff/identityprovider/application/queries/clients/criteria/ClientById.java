package lat.soulware.staff.identityprovider.application.queries.clients.criteria;

import java.util.UUID;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * One registered application, named by its registration's identity.
 *
 * @param id the registration's identity
 */
public record ClientById(UUID id) implements QueryCriteria {
}
