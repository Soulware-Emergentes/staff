package lat.soulware.staff.directory.domain.model.valueobjects;

import lat.soulware.staff.directory.domain.model.exceptions.BlankPasswordHashException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * The BCrypt hash of a person's password.
 *
 * @param value the text, never blank
 */
public record PasswordHash(String value) implements ValueObject {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new BlankPasswordHashException();
        }
    }
}
