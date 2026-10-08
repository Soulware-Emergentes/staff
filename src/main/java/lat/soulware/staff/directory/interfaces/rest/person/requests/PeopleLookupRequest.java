package lat.soulware.staff.directory.interfaces.rest.person.requests;

import java.util.List;
import java.util.UUID;

/**
 * The directory GUIDs to resolve.
 *
 * @param ids the GUIDs, at most 100
 */
public record PeopleLookupRequest(List<UUID> ids) {
}
