package lat.soulware.staff.directory.domain.model.valueobjects;

import lat.soulware.staff.directory.domain.model.exceptions.BlankUsernameException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * The account name a person logs in with.
 *
 * @param value the text, never blank
 */
public record Username(String value) implements ValueObject {

    public Username {
        if (value == null || value.isBlank()) {
            throw new BlankUsernameException();
        }
    }
}
