package lat.soulware.staff.directory.application.queries.groups.projections;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.directory.application.queries.groups.criteria.GroupsOfPerson;
import lat.soulware.staff.shared.application.queries.projections.Projection;

/** Group membership. Read from the database now, and from the directory's memberOf once it is LDAP. */
public interface GroupMembershipProjection extends Projection {

    /**
     * @param criteria whose groups to read
     * @return the objectGUIDs of the groups the person belongs to directly
     */
    List<UUID> findGroupIds(GroupsOfPerson criteria);
}
