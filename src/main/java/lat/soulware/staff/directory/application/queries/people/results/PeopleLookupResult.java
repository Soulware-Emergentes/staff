package lat.soulware.staff.directory.application.queries.people.results;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * The outcome of resolving a set of directory GUIDs.
 *
 * @param found    the people holding a requested GUID
 * @param notFound the requested GUIDs nobody in the directory holds, in the order requested
 */
public record PeopleLookupResult(List<PersonResult> found, List<UUID> notFound) implements QueryResult {

    public PeopleLookupResult {
        found = List.copyOf(found);
        notFound = List.copyOf(notFound);
    }
}
