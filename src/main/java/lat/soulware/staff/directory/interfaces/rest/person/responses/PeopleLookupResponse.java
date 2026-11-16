package lat.soulware.staff.directory.interfaces.rest.person.responses;

import java.util.List;
import java.util.UUID;

/**
 * The outcome of resolving a set of directory GUIDs.
 *
 * @param found    the people holding a requested GUID
 * @param notFound the requested GUIDs nobody in the directory holds
 */
public record PeopleLookupResponse(List<PersonResponse> found, List<UUID> notFound) {
}
