package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client registration is given no client id. */
public final class MissingClientIdException extends BusinessRuleViolationException {

    public MissingClientIdException() {
        super();
    }
}
