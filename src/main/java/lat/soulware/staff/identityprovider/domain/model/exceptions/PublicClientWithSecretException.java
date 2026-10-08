package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a public client is registered with a secret, which it could not keep. */
public final class PublicClientWithSecretException extends BusinessRuleViolationException {

    public PublicClientWithSecretException() {
        super();
    }
}
