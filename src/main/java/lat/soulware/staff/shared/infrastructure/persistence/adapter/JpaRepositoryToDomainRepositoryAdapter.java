package lat.soulware.staff.shared.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;

import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;
import lat.soulware.staff.shared.domain.repositories.DomainRepository;

/**
 * Base class implementing the {@link DomainRepository} port over Spring Data JPA.
 *
 * <p>Spring Data is held behind {@link #delegate()} rather than inherited, reachable from inside
 * the adapter and nowhere else.
 *
 * <p>All four port methods are implemented here and final. A subclass supplies the six abstract
 * hooks below, and overrides {@link #persist} where writing takes more than handing the entity to
 * the delegate.
 *
 * <p>The identity bound narrows to {@link AggregateId}{@code <K>}, so {@code id.value()} feeds
 * Spring Data with no conversion.
 *
 * @param <A>  aggregate root type
 * @param <ID> aggregate identity type
 * @param <E>  JPA entity type
 * @param <K>  primary key type, which is also what the identity wraps
 */
public abstract class JpaRepositoryToDomainRepositoryAdapter<
    A extends AggregateRoot<ID>,
    ID extends AggregateId<K>,
    E,
    K
> implements DomainRepository<A, ID> {

    /** Restricts construction to subclasses. The adapter holds no state of its own. */
    protected JpaRepositoryToDomainRepositoryAdapter() {
    }

    /**
     * The Spring Data repository this adapter delegates to. A subclass may narrow the return type
     * to its own repository interface, keeping its derived queries reachable from here.
     *
     * @return the delegate repository
     */
    protected abstract JpaRepository<E, K> delegate();

    /**
     * The publisher each drained event is handed to. Spring supplies one, so a subclass injects it
     * and returns it here.
     *
     * @return the application event publisher
     */
    protected abstract ApplicationEventPublisher publisher();

    /**
     * Maps an aggregate onto the row that stores it.
     *
     * @param aggregate the aggregate to write
     * @return the persistence entity holding its state
     */
    protected abstract E toEntity(A aggregate);

    /**
     * Reconstitutes an aggregate from persisted state. The result must satisfy every invariant the
     * aggregate enforces.
     *
     * @param entity the persistence entity read back
     * @return the aggregate it represents
     */
    protected abstract A toAggregate(E entity);

    /**
     * Reads the primary key off a persistence entity. Used to work out which of the requested
     * identities a bulk load failed to return.
     *
     * @param entity the persistence entity
     * @return its primary key
     */
    protected abstract K keyOf(E entity);

    /**
     * Builds this aggregate's own {@link EntityNotFoundException}. Called by both lookups when an
     * identity matches nothing.
     *
     * @param missing every identity that matched nothing, never empty
     * @return the exception to throw
     */
    protected abstract EntityNotFoundException notFound(Collection<ID> missing);

    /**
     * Writes the mapped entity. Overridable, for an aggregate whose optimistic-locking version
     * needs reconciling against the detached instance the mapping produces.
     *
     * @param entity the persistence entity to write
     * @return the entity as it stands once written, generated state included
     */
    protected E persist(E entity) {
        return this.delegate().save(entity);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Maps the aggregate, writes it through {@link #persist}, and maps back what was written.
     *
     * <p>The events registered on {@code aggregate} are drained once the write returns and handed
     * to {@link #publisher()} in registration order, so a write that throws announces nothing. The
     * returned instance is reconstituted from stored state and carries no events.
     */
    @Override
    public final A save(A aggregate) {
        E written = this.persist(this.toEntity(aggregate));
        A saved = this.toAggregate(written);

        aggregate.pullDomainEvents()
            .forEach(this.publisher()::publishEvent);

        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Looks up by {@code id.value()} and maps the result. A miss goes through {@link #notFound}
     * with a single-element collection.
     */
    @Override
    public final A getById(ID id) {
        return this.delegate().findById(id.value())
            .map(this::toAggregate)
            .orElseThrow(() -> this.notFound(List.of(id)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>One query for the whole set. Duplicates collapse: the requested identities are put through
     * a set first, which is also what the returned count is compared against.
     */
    @Override
    public final List<A> getAllByIds(Collection<ID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Set<ID> requested = Set.copyOf(ids);
        List<K> keys = requested.stream()
            .map(aggregateId -> aggregateId.value())
            .toList();
        List<E> found = this.delegate().findAllById(keys);

        boolean allPresent = found.size() == requested.size();
        if (!allPresent) {
            Set<K> foundKeys = found.stream()
                .map(this::keyOf)
                .collect(Collectors.toSet());
            List<ID> missing = requested.stream()
                .filter(id -> !foundKeys.contains(id.value()))
                .toList();
            throw this.notFound(missing);
        }

        return found.stream()
            .map(this::toAggregate)
            .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Physical unless the persistence entity says otherwise. {@code @SQLDelete} replaces the
     * statement Hibernate emits for this delete, and {@code @SQLRestriction} keeps the stamped row
     * out of every later read.
     */
    @Override
    public final void delete(A aggregate) {
        this.delegate().delete(this.toEntity(aggregate));
    }
}
