package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.DirectoryApi;
import lat.soulware.staff.identityprovider.application.queries.grants.criteria.RolesOfGroups;
import lat.soulware.staff.identityprovider.application.queries.grants.projections.GrantProjection;

/**
 * Lets a person into an application only when their groups hold a role on the API the login asks
 * for, which is what "assignment required" means in Entra ID. A login names exactly one API, and a
 * role to act as only when the person holds it. Anything else is answered {@code access_denied} or
 * {@code invalid_scope}, and no code is issued.
 *
 * <p>Runs after Spring's own checks of the redirect URI and the scopes. Before the person has logged
 * in there is nobody to check, so it lets the request through to the login form.
 */
@Component
public class EligibilityValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private final DirectoryApi directoryApi;
    private final GrantProjection grantProjection;

    public EligibilityValidator(DirectoryApi directoryApi, GrantProjection grantProjection) {
        this.directoryApi = directoryApi;
        this.grantProjection = grantProjection;
    }

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        OAuth2AuthorizationCodeRequestAuthenticationToken request = context.getAuthentication();
        Authentication person = (Authentication) request.getPrincipal();
        boolean hasLoggedIn = Objects.nonNull(person) && person.isAuthenticated();
        if (!hasLoggedIn) {
            return;
        }

        Collection<String> clientResources = context.getRegisteredClient()
            .getClientSettings()
            .getSetting(RequestedAccess.RESOURCES_SETTING);
        RequestedAccess access = RequestedAccess.of(request.getScopes(), clientResources);
        String resource = access.resource()
            .orElseThrow(() -> refuse(OAuth2ErrorCodes.INVALID_SCOPE, "Name exactly one API", request));

        List<UUID> groupIds = this.directoryApi.groupsOf(UUID.fromString(person.getName()));
        List<String> eligibleRoles = this.grantProjection.findRoles(new RolesOfGroups(resource, groupIds));
        if (eligibleRoles.isEmpty()) {
            throw refuse(OAuth2ErrorCodes.ACCESS_DENIED, "No role on " + resource, request);
        }
        Optional<String> actAs = access.actAs();
        boolean actsAsAHeldRole = actAs.isEmpty() || eligibleRoles.contains(actAs.get());
        if (!actsAsAHeldRole) {
            throw refuse(OAuth2ErrorCodes.ACCESS_DENIED, "Role not held on " + resource, request);
        }
    }

    private static OAuth2AuthorizationCodeRequestAuthenticationException refuse(
        String errorCode,
        String description,
        OAuth2AuthorizationCodeRequestAuthenticationToken request
    ) {
        return new OAuth2AuthorizationCodeRequestAuthenticationException(
            new OAuth2Error(errorCode, description, null),
            request
        );
    }
}
