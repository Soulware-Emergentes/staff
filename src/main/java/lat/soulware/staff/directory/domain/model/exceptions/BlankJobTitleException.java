package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.directory.domain.model.valueobjects.JobTitle} is blank. */
public final class BlankJobTitleException extends BusinessRuleViolationException {

    public BlankJobTitleException() {
        super();
    }
}
