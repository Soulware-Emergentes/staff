package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidClientIdException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * The public name of a registered application, such as {@code sgt-web-dev}.
 *
 * @param value one word, never blank
 */
public record ClientId(String value) implements ValueObject {

    public ClientId {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidClientIdException(value);
        }
    }
}
