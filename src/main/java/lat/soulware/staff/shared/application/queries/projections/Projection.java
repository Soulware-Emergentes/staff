package lat.soulware.staff.shared.application.queries.projections;

/**
 * Marker interface for projections. A projection is the port one read slice declares over the read
 * store: it names the rows that slice needs and says nothing about how they are fetched.
 *
 * <p>Implement as an interface, in the slice whose reads it serves, and let an adapter under
 * {@code infrastructure} supply it.
 */
public interface Projection {
}
