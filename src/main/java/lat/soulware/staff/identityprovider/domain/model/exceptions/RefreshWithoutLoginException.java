package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a client may renew tokens but cannot log anyone in, so it has none to renew. */
public final class RefreshWithoutLoginException extends BusinessRuleViolationException {

    public RefreshWithoutLoginException() {
        super();
    }
}
