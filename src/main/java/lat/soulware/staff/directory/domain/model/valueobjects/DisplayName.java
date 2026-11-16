package lat.soulware.staff.directory.domain.model.valueobjects;

import lat.soulware.staff.directory.domain.model.exceptions.BlankDisplayNameException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * A person's name as shown to others.
 *
 * @param value the text, never blank
 */
public record DisplayName(String value) implements ValueObject {

    public DisplayName {
        if (value == null || value.isBlank()) {
            throw new BlankDisplayNameException();
        }
    }
}
