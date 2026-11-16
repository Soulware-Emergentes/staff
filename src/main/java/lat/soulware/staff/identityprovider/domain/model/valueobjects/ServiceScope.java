package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import lat.soulware.staff.identityprovider.domain.model.exceptions.InvalidServiceScopeException;
import lat.soulware.staff.shared.domain.model.valueobjects.ValueObject;

/**
 * A permission an API defines for services calling it on their own behalf, such as {@code tasks.write}.
 *
 * @param value one word, never blank
 */
public record ServiceScope(String value) implements ValueObject {

    public ServiceScope {
        if (value == null || value.isBlank() || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidServiceScopeException(value);
        }
    }
}
