package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientId} is blank or holds whitespace. */
public final class InvalidClientIdException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidClientIdException(String value) {
        super(value);
    }
}
