package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidRedirectUriException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * An address the identity provider may send a registered application back to.
 *
 * @param value an absolute URI, never blank
 */
public record RedirectUri(String value) implements ValueObject {

    public RedirectUri {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidRedirectUriException(value);
        }
    }
}
