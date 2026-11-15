package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/** A way a registered application may get tokens, independent of whether it can keep a secret. */
public enum ClientGrantType implements ValueObject {
    /** Logs a person in through the browser and gets tokens on their behalf. */
    AUTHORIZATION_CODE,
    /** Renews a person's tokens without sending them back to the login. */
    REFRESH_TOKEN,
    /** Gets tokens for the application itself, with no person involved. */
    CLIENT_CREDENTIALS
}
