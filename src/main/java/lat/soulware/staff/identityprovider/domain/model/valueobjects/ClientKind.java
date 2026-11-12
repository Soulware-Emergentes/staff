package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/** Whether a registered application can keep a secret. */
public enum ClientKind implements ValueObject {
    /** Runs where anyone can read its code, such as a browser. Proves itself with PKCE. */
    PUBLIC,
    /** Runs on a server and holds a secret, such as a backend calling another one. */
    CONFIDENTIAL
}
