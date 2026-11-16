package lat.soulware.staff.directory.domain.model.valueobjects;

import lat.soulware.staff.directory.domain.model.exceptions.BlankDepartmentException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * Where in the organization a person works, as the directory states it.
 *
 * @param value the text, never blank
 */
public record Department(String value) implements ValueObject {

    public Department {
        if (value == null || value.isBlank()) {
            throw new BlankDepartmentException();
        }
    }
}
