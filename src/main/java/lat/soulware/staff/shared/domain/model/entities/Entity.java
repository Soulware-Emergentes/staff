package lat.soulware.staff.shared.domain.model.entities;

import java.util.Objects;

import lat.soulware.staff.shared.domain.model.valueobjects.EntityId;

/**
 * Base class for domain entities. Identity-based equality: two entities are equal if and only if
 * their identities are equal, regardless of other field values.
 *
 * @param <ID> identity type
 */
public abstract class Entity<ID extends EntityId<?>> {

    /** Restricts construction to subclasses. An entity only ever exists as a concrete type. */
    protected Entity() {
    }

    /**
     * This entity's identity. Subclasses expose the field they were constructed with. The identity
     * is assigned once and never changes.
     *
     * @return the identity, or null while the entity has not been assigned one
     */
    public abstract ID getId();

    /**
     * Compares by identity and exact runtime class. Field values are ignored, so an entity loaded
     * from the database equals the same entity after any number of mutations.
     *
     * <p>An entity with no identity yet equals only itself.
     *
     * @param o the object to compare against
     * @return true when both are the same class and carry equal, non-null identities
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return this.getId() != null && Objects.equals(this.getId(), entity.getId());
    }

    /**
     * Hashes the identity, falling back to the JVM identity hash while there is none. An entity's
     * hash therefore changes when an identity is assigned.
     *
     * @return the identity's hash, or the JVM identity hash when no identity is set
     */
    @Override
    public final int hashCode() {
        return this.getId() != null ? Objects.hashCode(this.getId()) : System.identityHashCode(this);
    }
}
