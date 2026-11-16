package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidAppRoleException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * A role an API defines for the people calling it, such as {@code reviewer}. Means something to that API alone.
 *
 * @param value one word, never blank
 */
public record AppRole(String value) implements ValueObject {

    public AppRole {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidAppRoleException(value);
        }
    }
}
