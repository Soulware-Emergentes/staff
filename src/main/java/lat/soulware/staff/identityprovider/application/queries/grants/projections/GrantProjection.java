package lat.soulware.staff.identityprovider.application.queries.grants.projections;

import java.util.List;

import lat.soulware.staff.identityprovider.application.queries.grants.criteria.RolesOfGroups;
import lat.soulware.staff.identityprovider.application.queries.grants.criteria.ScopesOfClient;
import lat.soulware.staff.shared.application.queries.projections.Projection;

/** What the identity provider grants: roles to groups of people, and scopes to applications. */
public interface GrantProjection extends Projection {

    /**
     * @param criteria the API and the groups
     * @return the roles any of the groups holds on the API, distinct and in order, empty when none
     */
    List<String> findRoles(RolesOfGroups criteria);

    /**
     * @param criteria the application and the API
     * @return the scopes granted to it on the API, distinct and in order, empty when none
     */
    List<String> findScopes(ScopesOfClient criteria);
}
