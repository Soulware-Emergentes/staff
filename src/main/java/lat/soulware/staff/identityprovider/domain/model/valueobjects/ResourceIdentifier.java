package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidResourceIdentifierException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * The name an API is known by in tokens: the audience a token for it carries, such as {@code sgt-api-dev}. Also the scope a client requests to get such a token.
 *
 * @param value one word, never blank
 */
public record ResourceIdentifier(String value) implements ValueObject {

    public ResourceIdentifier {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidResourceIdentifierException(value);
        }
    }
}
