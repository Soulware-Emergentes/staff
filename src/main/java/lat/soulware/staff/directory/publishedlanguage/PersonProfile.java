package lat.soulware.staff.directory.publishedlanguage;

import java.util.Objects;
import java.util.UUID;

import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * Who a person is, as the directory describes them: the claims an identity provider puts in an ID
 * token. Nothing here grants anything in any application.
 *
 * @param id          the objectGUID of their account
 * @param username    the account name they log in with
 * @param displayName their name as shown to others
 * @param jobTitle    what they do in the organization
 * @param department  where in the organization they do it
 */
public record PersonProfile(
    UUID id,
    String username,
    String displayName,
    String jobTitle,
    String department
) implements ValueObject {

    public PersonProfile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(jobTitle, "jobTitle");
        Objects.requireNonNull(department, "department");
    }
}
