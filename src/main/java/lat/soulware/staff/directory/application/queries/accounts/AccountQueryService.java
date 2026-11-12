package lat.soulware.staff.directory.application.queries.accounts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lat.soulware.staff.directory.application.queries.accounts.criteria.Credentials;
import lat.soulware.staff.directory.application.queries.accounts.projections.CredentialProjection;
import lat.soulware.staff.directory.application.queries.accounts.results.AuthenticationResult;
import lat.soulware.staff.shared.application.queries.QueryService;

/** Checks the credentials a person logs in with. */
@Service
@Transactional(readOnly = true)
public class AccountQueryService implements QueryService {

    private final CredentialProjection credentialProjection;

    public AccountQueryService(CredentialProjection credentialProjection) {
        this.credentialProjection = credentialProjection;
    }

    public AuthenticationResult authenticate(Credentials criteria) {
        return this.credentialProjection.findAuthenticated(criteria)
            .map(personId -> new AuthenticationResult(true, personId))
            .orElseGet(() -> new AuthenticationResult(false, null));
    }
}
