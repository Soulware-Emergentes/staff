package lat.soulware.staff.shared.domain.model.valueobjects;

/**
 * A typed identity. Each entity declares its own id type.
 *
 * <p>{@code T} is the underlying key type, chosen by the entity: {@code UUID}, {@code Long}, or a
 * natural key.
 *
 * <p>Implement as a {@code record}. Enforce the null check in a compact constructor.
 *
 * @param <T> underlying key type
 */
public interface EntityId<T> extends ValueObject {

    /**
     * The underlying key this identity wraps. Unwrap only at a boundary that cannot hold the typed
     * identity: a database column, a URL path segment, a response body. Domain code passes the
     * identity itself.
     *
     * @return the key value, never null
     */
    T value();
}
