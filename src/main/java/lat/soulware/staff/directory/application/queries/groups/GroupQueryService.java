package lat.soulware.staff.directory.application.queries.groups;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lat.soulware.staff.directory.application.queries.groups.criteria.GroupsOfPerson;
import lat.soulware.staff.directory.application.queries.groups.projections.GroupMembershipProjection;
import lat.soulware.staff.directory.application.queries.groups.results.GroupMembershipResult;
import lat.soulware.staff.shared.application.queries.QueryService;

/** Answers which groups a person belongs to. */
@Service
@Transactional(readOnly = true)
public class GroupQueryService implements QueryService {

    private final GroupMembershipProjection groupMembershipProjection;

    public GroupQueryService(GroupMembershipProjection groupMembershipProjection) {
        this.groupMembershipProjection = groupMembershipProjection;
    }

    public GroupMembershipResult groupsOf(GroupsOfPerson criteria) {
        return new GroupMembershipResult(this.groupMembershipProjection.findGroupIds(criteria));
    }
}
