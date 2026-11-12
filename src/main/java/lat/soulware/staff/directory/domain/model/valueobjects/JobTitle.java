package lat.soulware.staff.directory.domain.model.valueobjects;

import lat.soulware.staff.directory.domain.model.exceptions.BlankJobTitleException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * What a person does in the organization, as the directory states it.
 *
 * @param value the text, never blank
 */
public record JobTitle(String value) implements ValueObject {

    public JobTitle {
        if (value == null || value.isBlank()) {
            throw new BlankJobTitleException();
        }
    }
}
