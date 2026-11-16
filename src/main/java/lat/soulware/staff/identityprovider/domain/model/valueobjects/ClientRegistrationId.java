package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import java.util.UUID;

import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingClientRegistrationIdException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Identity of a {@link lat.soulware.staff.identityprovider.domain.model.aggregates.ClientRegistration}.
 *
 * @param value the underlying key, never null
 */
public record ClientRegistrationId(UUID value) implements AggregateId<UUID> {

    public ClientRegistrationId {
        if (value == null) {
            throw new MissingClientRegistrationIdException();
        }
    }
}
