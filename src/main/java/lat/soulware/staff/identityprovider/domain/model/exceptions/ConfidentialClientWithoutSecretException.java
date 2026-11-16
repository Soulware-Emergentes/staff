package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a confidential client is registered without a secret. */
public final class ConfidentialClientWithoutSecretException extends BusinessRuleViolationException {

    public ConfidentialClientWithoutSecretException() {
        super();
    }
}
