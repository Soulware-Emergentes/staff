package lat.soulware.staff.shared.application.queries;

/**
 * Marker interface for query services. A query service answers the reads of one slice: it takes the
 * slice's criteria, goes through its projections, and hands back its results.
 *
 * <p>Implement as a class carrying {@code @Transactional(readOnly = true)}, whose methods return
 * the slice's own result records.
 */
public interface QueryService {
}
