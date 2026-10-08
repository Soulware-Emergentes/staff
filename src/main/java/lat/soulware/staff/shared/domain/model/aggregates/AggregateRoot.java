package lat.soulware.staff.shared.domain.model.aggregates;

import java.util.ArrayList;
import java.util.List;

import lat.soulware.staff.shared.domain.model.entities.Entity;
import lat.soulware.staff.shared.domain.model.events.DomainEvent;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Base class for aggregate roots. Aggregates are the consistency boundaries: invariants are
 * enforced within a single aggregate, and cross-aggregate communication happens through domain
 * events.
 *
 * <p>A domain method that changes state calls {@link #registerEvent} to record what happened. The
 * persistence adapter drains the aggregate with {@link #pullDomainEvents} once the write returns
 * and hands each event to Spring's {@code ApplicationEventPublisher}. The domain never publishes
 * anything itself.
 *
 * @param <ID> identity type
 */
public abstract class AggregateRoot<ID extends AggregateId<?>> extends Entity<ID> {

    /** Facts recorded since the last drain, in registration order. */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** Restricts construction to subclasses. A newly built root has recorded nothing yet. */
    protected AggregateRoot() {
    }

    /**
     * Records an event for publication once the aggregate has been saved. Call this from the same
     * domain method that made the change.
     *
     * <p>Registering does not publish. An aggregate that is never saved announces nothing.
     *
     * @param event the fact to announce, named in past tense
     */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Returns the events recorded so far and clears them, so a second call returns nothing. The
     * persistence adapter calls this after a successful write and publishes what it receives.
     *
     * @return the recorded events in registration order, empty when nothing was recorded
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}
