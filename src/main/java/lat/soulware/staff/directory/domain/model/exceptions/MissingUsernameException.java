package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a person is given no username. */
public final class MissingUsernameException extends BusinessRuleViolationException {

    public MissingUsernameException() {
        super();
    }
}
