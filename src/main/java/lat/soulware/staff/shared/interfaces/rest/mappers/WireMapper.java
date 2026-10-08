package lat.soulware.staff.shared.interfaces.rest.mappers;

/**
 * Marker interface for the mappers that convert across the wire, in both directions. Inbound, a
 * mapper turns the raw fields of a request into the type the application accepts: a command on the
 * write side, a criterion on the read side, and it is where a {@code String} becomes a value object.
 * Outbound, it unwraps a query result onto the response the controller answers with.
 *
 * <p>Implement as a class in the {@code mappers} package of the aggregate whose controller it
 * serves, named for that aggregate plus this interface's name. One mapper per controller, with one
 * method per conversion that controller needs, each named for what it produces.
 *
 * <p>The methods are static and the class holds no state, so a controller calls them without
 * injecting anything.
 */
public interface WireMapper {
}
