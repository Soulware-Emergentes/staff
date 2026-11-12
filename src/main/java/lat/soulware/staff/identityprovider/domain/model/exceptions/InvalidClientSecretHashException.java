package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.ClientSecretHash} is blank or holds whitespace. */
public final class InvalidClientSecretHashException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidClientSecretHashException(String value) {
        super(value);
    }
}
