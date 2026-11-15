package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client logs people in but names nowhere a login may return to. */
public final class LoginWithoutRedirectUriException extends BusinessRuleViolationException {

    public LoginWithoutRedirectUriException() {
        super();
    }
}
