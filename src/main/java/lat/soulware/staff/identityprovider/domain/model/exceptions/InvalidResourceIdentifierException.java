package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.ResourceIdentifier} is blank or holds whitespace. */
public final class InvalidResourceIdentifierException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidResourceIdentifierException(String value) {
        super(value);
    }
}
