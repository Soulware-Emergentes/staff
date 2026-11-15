package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client is registered without any way to get tokens. */
public final class MissingClientGrantTypesException extends BusinessRuleViolationException {

    public MissingClientGrantTypesException() {
        super();
    }
}
