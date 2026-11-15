package lat.soulware.staff.identityprovider.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when a public client is given a grant only a client holding a secret may use. */
public final class PublicClientWithConfidentialGrantException extends BusinessRuleViolationException {

    /**
     * @param grantType the grant it was given
     */
    public PublicClientWithConfidentialGrantException(String grantType) {
        super(grantType);
    }
}
