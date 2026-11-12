package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.AppRole} is blank or holds whitespace. */
public final class InvalidAppRoleException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidAppRoleException(String value) {
        super(value);
    }
}
