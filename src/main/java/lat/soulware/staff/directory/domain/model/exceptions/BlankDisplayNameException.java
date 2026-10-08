package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.directory.domain.model.valueobjects.DisplayName} is blank. */
public final class BlankDisplayNameException extends BusinessRuleViolationException {

    public BlankDisplayNameException() {
        super();
    }
}
