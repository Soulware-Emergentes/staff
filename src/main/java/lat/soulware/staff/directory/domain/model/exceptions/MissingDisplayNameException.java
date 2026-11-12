package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a person is given no display name. */
public final class MissingDisplayNameException extends BusinessRuleViolationException {

    public MissingDisplayNameException() {
        super();
    }
}
