package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a person is given no department. */
public final class MissingDepartmentException extends BusinessRuleViolationException {

    public MissingDepartmentException() {
        super();
    }
}
