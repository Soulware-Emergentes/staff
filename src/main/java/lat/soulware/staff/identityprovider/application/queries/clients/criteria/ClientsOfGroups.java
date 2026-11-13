package lat.soulware.staff.identityprovider.application.queries.clients.criteria;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * The applications a set of groups can be logged into: the public clients of the APIs any of them
 * holds a role on.
 *
 * @param groupIds the objectGUIDs of the groups, as the directory names them
 */
public record ClientsOfGroups(List<UUID> groupIds) implements QueryCriteria {

    public ClientsOfGroups {
        groupIds = List.copyOf(groupIds);
    }
}
