package lat.soulware.staff.directory.domain.model.exceptions;

import java.util.Collection;

import lat.soulware.staff.directory.domain.model.valueobjects.PersonId;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;

/** Thrown when no person carries the requested identity. */
public final class PersonNotFoundException extends EntityNotFoundException {

    /**
     * @param missing the identities that matched nothing
     */
    public PersonNotFoundException(Collection<PersonId> missing) {
        super(missing);
    }
}
