package lat.soulware.staff.directory.domain.model.valueobjects;

import java.util.UUID;

import lat.soulware.staff.directory.domain.model.exceptions.MissingPersonIdException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Identity of a {@link lat.soulware.staff.directory.domain.model.aggregates.Person}: the objectGUID of their directory account.
 *
 * @param value the underlying key, never null
 */
public record PersonId(UUID value) implements AggregateId<UUID> {

    public PersonId {
        if (value == null) {
            throw new MissingPersonIdException();
        }
    }
}
