package lat.soulware.staff.shared.domain.repositories;

import java.util.Collection;
import java.util.List;

import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Contract every domain repository extends. One aggregate root, one repository. The interface lives
 * in the domain layer and the adapter implementing it lives in infrastructure.
 *
 * <p>Both lookups throw rather than return nothing: a caller handing over an identity gets the
 * aggregate or an {@link EntityNotFoundException}.
 *
 * <p>A repository whose aggregate needs a lookup that tolerates absence declares its own and names
 * it {@code find...}. The convention: {@code get} must succeed, {@code find} may come back empty.
 *
 * @param <A>  aggregate root type
 * @param <ID> aggregate identity type
 */
public interface DomainRepository<A extends AggregateRoot<ID>, ID extends AggregateId<?>> {

    /**
     * Writes the whole aggregate, inserting or updating as needed. Everything inside the boundary
     * is written together.
     *
     * @param aggregate the aggregate to store
     * @return the stored aggregate, which may differ from the argument once the adapter has applied
     *         generated state such as a version
     */
    A save(A aggregate);

    /**
     * Loads the aggregate carrying {@code id}.
     *
     * @param id the identity to look up
     * @return the aggregate, never null
     * @throws EntityNotFoundException if no aggregate carries that identity
     */
    A getById(ID id);

    /**
     * Loads every aggregate named in {@code ids}, in one round trip, and fails unless all of them
     * were found.
     *
     * <p>Duplicate identities collapse, so passing the same one twice yields one aggregate and does
     * not fail. An empty collection returns an empty list without querying.
     *
     * @param ids the identities to load
     * @return the aggregates, in unspecified order, one per distinct identity
     * @throws EntityNotFoundException if any identity matched nothing
     */
    List<A> getAllByIds(Collection<ID> ids);

    /**
     * Removes the aggregate and everything inside its boundary. Interior entities go with it.
     *
     * @param aggregate the aggregate to remove
     */
    void delete(A aggregate);
}
