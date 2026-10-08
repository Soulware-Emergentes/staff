package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.directory.domain.model.valueobjects.PasswordHash} is blank. */
public final class BlankPasswordHashException extends BusinessRuleViolationException {

    public BlankPasswordHashException() {
        super();
    }
}
