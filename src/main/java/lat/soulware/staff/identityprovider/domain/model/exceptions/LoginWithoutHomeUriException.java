package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client logs people in but has no home URI for the hub to open it at. */
public final class LoginWithoutHomeUriException extends BusinessRuleViolationException {

    public LoginWithoutHomeUriException() {
        super();
    }
}
