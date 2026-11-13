package lat.soulware.staff.identityprovider.application.queries.clients.projections;

import java.util.List;
import java.util.Optional;

import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientById;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientsOfGroups;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;
import lat.soulware.staff.identityprovider.application.queries.clients.results.LaunchableClientResult;
import lat.soulware.staff.shared.application.queries.projections.Projection;

/** The applications registered with the identity provider. */
public interface ClientProjection extends Projection {

    /**
     * @param criteria the client id to read
     * @return the registration, empty when no application uses that client id
     */
    Optional<ClientResult> findByClientId(ClientByClientId criteria);

    /**
     * @param criteria the registration to read
     * @return the registration, empty when none carries that identity
     */
    Optional<ClientResult> findById(ClientById criteria);

    /**
     * @param criteria the groups
     * @return the public clients of the APIs any of the groups holds a role on, by environment name and
     *     then application name, empty when none
     */
    List<LaunchableClientResult> findLaunchable(ClientsOfGroups criteria);
}
