package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a person is given no identity. */
public final class MissingPersonIdException extends BusinessRuleViolationException {

    public MissingPersonIdException() {
        super();
    }
}
