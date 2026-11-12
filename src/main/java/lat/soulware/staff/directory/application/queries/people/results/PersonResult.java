package lat.soulware.staff.directory.application.queries.people.results;

import java.util.UUID;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * A person as other services know them.
 *
 * @param id          the objectGUID of their account
 * @param username    the account name they log in with
 * @param displayName their name as shown to others
 * @param jobTitle    what they do in the organization
 * @param department  where in the organization they do it
 */
public record PersonResult(
    UUID id,
    String username,
    String displayName,
    String jobTitle,
    String department
) implements QueryResult {
}
