package lat.soulware.staff.directory.interfaces.rest.person.responses;

import java.util.UUID;

/**
 * One person in the directory.
 *
 * @param id          the objectGUID of their account
 * @param username    the account name they log in with
 * @param displayName their name as shown to others
 * @param jobTitle    what they do in the organization
 * @param department  where in the organization they do it
 */
public record PersonResponse(UUID id, String username, String displayName, String jobTitle, String department) {
}
