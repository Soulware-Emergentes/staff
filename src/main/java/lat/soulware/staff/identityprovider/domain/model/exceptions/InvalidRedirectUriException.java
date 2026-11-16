package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a {@link lat.soulware.staff.identityprovider.domain.model.valueobjects.RedirectUri} is blank or holds whitespace. */
public final class InvalidRedirectUriException extends BusinessRuleViolationException {

    /**
     * @param value the value given
     */
    public InvalidRedirectUriException(String value) {
        super(value);
    }
}
