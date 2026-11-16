package lat.soulware.staff.directory.application.queries.accounts.projections;

import java.util.Optional;
import java.util.UUID;

import lat.soulware.staff.directory.application.queries.accounts.criteria.Credentials;
import lat.soulware.staff.shared.application.queries.projections.Projection;

/**
 * The directory's password check. A hash comparison against the database now, and an LDAP bind once
 * the real directory is in place.
 */
public interface CredentialProjection extends Projection {

    /**
     * @param criteria what the person typed
     * @return the objectGUID of the account the credentials open, empty when they open none, or when
     *         the account has no password
     */
    Optional<UUID> findAuthenticated(Credentials criteria);
}
