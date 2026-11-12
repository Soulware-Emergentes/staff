package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidClientSecretHashException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * The BCrypt hash of a confidential client's secret.
 *
 * @param value the hash, never blank
 */
public record ClientSecretHash(String value) implements ValueObject {

    public ClientSecretHash {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidClientSecretHashException(value);
        }
    }
}
