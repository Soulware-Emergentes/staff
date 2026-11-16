package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.ServiceScope} is blank or holds whitespace. */
public final class InvalidServiceScopeException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidServiceScopeException(String value) {
        super(value);
    }
}
