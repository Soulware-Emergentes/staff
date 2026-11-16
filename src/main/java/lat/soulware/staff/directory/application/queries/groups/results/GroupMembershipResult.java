package lat.soulware.staff.directory.application.queries.groups.results;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * The groups a person belongs to.
 *
 * @param groupIds the objectGUIDs of those groups, empty when none
 */
public record GroupMembershipResult(List<UUID> groupIds) implements QueryResult {

    public GroupMembershipResult {
        groupIds = List.copyOf(groupIds);
    }
}
