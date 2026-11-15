package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.HomeUri} is blank or holds whitespace. */
public final class InvalidHomeUriException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidHomeUriException(String value) {
        super(value);
    }
}
