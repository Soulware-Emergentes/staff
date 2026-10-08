package lat.soulware.staff.shared.domain.model.valueobjects;

/**
 * Marker interface for value objects. A value object is immutable, structurally equal, and has no
 * identity of its own. Implementors must:
 * <ul>
 *   <li>Be immutable: all fields final, no setters.</li>
 *   <li>Base equals and hashCode solely on their fields.</li>
 *   <li>Enforce their own invariants in the constructor.</li>
 * </ul>
 *
 * <p>Prefer a {@code record}, which satisfies the first two automatically.
 */
public interface ValueObject {
}
