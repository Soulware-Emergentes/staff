package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidHomeUriException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * Where an application that logs people in is opened from the hub.
 *
 * @param value an absolute URI, never blank
 */
public record HomeUri(String value) implements ValueObject {

    public HomeUri {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidHomeUriException(value);
        }
    }
}
