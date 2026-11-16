package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client registration does not say whether it can keep a secret. */
public final class MissingClientKindException extends BusinessRuleViolationException {

    public MissingClientKindException() {
        super();
    }
}
