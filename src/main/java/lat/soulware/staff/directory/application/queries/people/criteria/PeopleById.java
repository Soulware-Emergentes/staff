package lat.soulware.staff.directory.application.queries.people.criteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * The people holding the given directory GUIDs, whatever their job.
 *
 * @param ids the GUIDs to resolve, copied as given, a null entry included so the read can refuse it
 */
public record PeopleById(List<UUID> ids) implements QueryCriteria {

    public PeopleById {
        ids = Collections.unmodifiableList(new ArrayList<>(ids));
    }
}
