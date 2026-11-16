package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when an API resource is given no identifier. */
public final class MissingResourceIdentifierException extends BusinessRuleViolationException {

    public MissingResourceIdentifierException() {
        super();
    }
}
