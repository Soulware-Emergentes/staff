package lat.soulware.staff.shared.domain.model.valueobjects;

/**
 * Identity of an aggregate root, and the only identity another module is allowed to hold a
 * reference to.
 *
 * @param <T> underlying key type
 */
public interface AggregateId<T> extends EntityId<T> {
}
