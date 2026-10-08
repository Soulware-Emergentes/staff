package lat.soulware.staff.shared.domain.services;

/**
 * Marker interface for domain services. A domain service is a capability the domain needs and does
 * not own, stated as an operation in the model's own vocabulary (finding a route, quoting a price)
 * and silent on what supplies it. The adapter implementing it belongs in {@code infrastructure}.
 *
 * <p>Implement as an interface whose methods take and return value objects, entities, and
 * aggregates.
 */
public interface DomainService {
}
