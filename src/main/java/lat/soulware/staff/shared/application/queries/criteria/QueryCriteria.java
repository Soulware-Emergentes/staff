package lat.soulware.staff.shared.application.queries.criteria;

/**
 * Marker interface for query criteria. A criterion is the input to one read: the filters, the sort,
 * and the paging a caller asks for.
 *
 * <p>Implement as a {@code record} carrying raw types.
 *
 * <p>A criterion stops at the query service, which turns it into a call against the read store's
 * projections. Neither the domain nor any module's published language may appear in one; a read
 * spanning two contexts filters on the raw value its own store holds.
 */
public interface QueryCriteria {
}
