package lat.soulware.staff.shared;

import lat.soulware.staff.shared.domain.model.valueobjects.AggregateId;

/**
 * Marker interface for the API a module publishes to the others. It names the operations another
 * module may ask for, and sits at the module root.
 *
 * <p>Implement as an interface named for the module, plus {@code Api}, whose methods carry raw
 * types and the module's own {@code publishedlanguage}. Another module's
 * published type may appear only where it is an {@link AggregateId}. No aggregate and no value
 * object from {@code domain.model.valueobjects} may appear in those signatures.
 *
 * <p>The class satisfying it is an {@code *ApiService} in the application layer.
 */
public interface ModuleApi {
}
