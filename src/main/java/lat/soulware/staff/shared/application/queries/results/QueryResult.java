package lat.soulware.staff.shared.application.queries.results;

/**
 * Marker interface for query results. A result is the output of one read, shaped by the interface
 * that asked for it.
 *
 * <p>Implement as a {@code record} carrying raw types. Neither the domain nor any module's published
 * language may appear in a result.
 */
public interface QueryResult {
}
