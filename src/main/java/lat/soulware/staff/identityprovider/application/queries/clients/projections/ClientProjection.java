package lat.soulware.staff.identityprovider.application.queries.clients.projections;

import java.util.Optional;

import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientById;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;
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
}
