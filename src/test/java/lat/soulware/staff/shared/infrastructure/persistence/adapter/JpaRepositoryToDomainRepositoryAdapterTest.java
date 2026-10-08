package lat.soulware.staff.shared.infrastructure.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;

import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.events.DomainEvent;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;
import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Covers what {@code save} does with the events an aggregate registered, which is the one place the
 * template publishes anything: they leave on a successful write, in order, once each, and the
 * aggregate the caller gets back carries none.
 *
 * <p>The adapter under test writes through {@code persist} and never reaches a delegate, so these
 * run without a database.
 */
class JpaRepositoryToDomainRepositoryAdapterTest {

    private final ProbeAdapter adapter = new ProbeAdapter();

    @Test
    void publishesEveryEventTheAggregateRegistered() {
        Probe probe = new Probe();
        probe.change();
        probe.change();

        this.adapter.save(probe);

        assertEquals(2, this.adapter.published.size());
    }

    @Test
    void publishesAfterTheWrite() {
        Probe probe = new Probe();
        probe.change();

        this.adapter.save(probe);

        assertIterableEquals(List.of("write", "publish"), this.adapter.log);
    }

    @Test
    void publishesNothingWhenTheWriteThrows() {
        Probe probe = new Probe();
        probe.change();
        this.adapter.writeFails = true;

        assertThrows(IllegalStateException.class, () -> this.adapter.save(probe));

        assertTrue(this.adapter.published.isEmpty(), "a failed write announced something");
        assertEquals(1, probe.pullDomainEvents().size(), "the aggregate was drained anyway");
    }

    @Test
    void drainsTheAggregateSoASecondSaveAnnouncesNothing() {
        Probe probe = new Probe();
        probe.change();

        this.adapter.save(probe);
        this.adapter.save(probe);

        assertEquals(1, this.adapter.published.size());
    }

    @Test
    void returnsAnAggregateCarryingNoEvents() {
        Probe probe = new Probe();
        probe.change();

        Probe saved = this.adapter.save(probe);

        assertTrue(saved.pullDomainEvents().isEmpty());
    }

    /** Records what it was asked to write and what it was asked to announce, in the order asked. */
    private static final class ProbeAdapter
        extends JpaRepositoryToDomainRepositoryAdapter<Probe, ProbeId, ProbeRow, UUID> {

        private final List<String> log = new ArrayList<>();
        private final List<Object> published = new ArrayList<>();
        private boolean writeFails;

        @Override
        protected JpaRepository<ProbeRow, UUID> delegate() {
            throw new AssertionError("these tests write through persist and reach no delegate");
        }

        @Override
        protected ApplicationEventPublisher publisher() {
            return event -> {
                this.log.add("publish");
                this.published.add(event);
            };
        }

        @Override
        protected ProbeRow persist(ProbeRow entity) {
            if (this.writeFails) {
                throw new IllegalStateException("the write failed");
            }
            this.log.add("write");

            return entity;
        }

        @Override
        protected ProbeRow toEntity(Probe aggregate) {
            return new ProbeRow(aggregate.getId().value());
        }

        @Override
        protected Probe toAggregate(ProbeRow entity) {
            return new Probe(new ProbeId(entity.id()));
        }

        @Override
        protected UUID keyOf(ProbeRow entity) {
            return entity.id();
        }

        @Override
        protected EntityNotFoundException notFound(Collection<ProbeId> missing) {
            return new ProbeNotFoundException();
        }
    }

    /** An aggregate whose only behaviour is announcing that it changed. */
    private static final class Probe extends AggregateRoot<ProbeId> {

        private final ProbeId id;

        private Probe() {
            this(new ProbeId(UUID.randomUUID()));
        }

        private Probe(ProbeId id) {
            this.id = id;
        }

        @Override
        public ProbeId getId() {
            return this.id;
        }

        private void change() {
            this.registerEvent(new ProbeChanged(Instant.now()));
        }
    }

    /** The row the probe maps onto. */
    private record ProbeRow(UUID id) {
    }

    private record ProbeId(UUID value) implements AggregateId<UUID> {
    }

    private record ProbeChanged(Instant occurredOn) implements DomainEvent {
    }

    private static final class ProbeNotFoundException extends EntityNotFoundException {
    }
}