package lat.soulware.staff.shared.domain.model.events;

import java.time.Instant;

/**
 * Marker interface for domain events. A domain event names something that already happened. Name it
 * in past tense, and carry the data a listener needs without pointing back at the aggregate.
 *
 * <p>Implement as a {@code record}, with {@link #occurredOn()} satisfied by a component of that
 * name.
 */
public interface DomainEvent {

    /**
     * When the change happened, set by the aggregate at the moment it recorded the event. A delayed
     * or retried delivery still reports the original instant.
     *
     * @return the instant the change occurred
     */
    Instant occurredOn();
}
