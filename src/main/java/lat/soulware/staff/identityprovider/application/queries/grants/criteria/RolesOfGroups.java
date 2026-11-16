package lat.soulware.staff.identityprovider.application.queries.grants.criteria;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * The roles a set of groups holds on one API.
 *
 * @param resource the API's identifier
 * @param groupIds the objectGUIDs of the groups, as the directory names them
 */
public record RolesOfGroups(String resource, List<UUID> groupIds) implements QueryCriteria {

    public RolesOfGroups {
        groupIds = List.copyOf(groupIds);
    }
}
