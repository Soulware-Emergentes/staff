package lat.soulware.staff.directory.application.queries.groups.criteria;

import java.util.UUID;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * The groups one person belongs to.
 *
 * @param personId the objectGUID of the person's account
 */
public record GroupsOfPerson(UUID personId) implements QueryCriteria {
}
