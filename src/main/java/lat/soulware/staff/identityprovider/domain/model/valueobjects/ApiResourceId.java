package lat.soulware.staff.identityprovider.domain.model.valueobjects;

import java.util.UUID;

import lat.soulware.staff.identityprovider.domain.model.exceptions.MissingApiResourceIdException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Identity of a {@link lat.soulware.staff.identityprovider.domain.model.aggregates.ApiResource}.
 *
 * @param value the underlying key, never null
 */
public record ApiResourceId(UUID value) implements AggregateId<UUID> {

    public ApiResourceId {
        if (value == null) {
            throw new MissingApiResourceIdException();
        }
    }
}
