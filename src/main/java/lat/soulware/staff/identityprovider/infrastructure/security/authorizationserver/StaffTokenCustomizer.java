package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.DirectoryApi;
import lat.soulware.staff.directory.publishedlanguage.PersonProfile;
import lat.soulware.staff.identityprovider.application.queries.grants.criteria.RolesOfGroups;
import lat.soulware.staff.identityprovider.application.queries.grants.criteria.ScopesOfClient;
import lat.soulware.staff.identityprovider.application.queries.grants.projections.GrantProjection;

/**
 * Shapes the tokens the way an identity provider in front of a directory does, keeping what a person
 * may do apart from who they are:
 *
 * <ul>
 *   <li>A person's access token is addressed to the one API the login named. Its subject is their
 *       objectGUID, {@code roles} holds the one role active, and {@code eligible_roles} every role
 *       their groups hold there, so a frontend can offer to switch.</li>
 *   <li>A service's access token is addressed to the API it asked for, its subject is its client id,
 *       and {@code scope} holds only what was granted to it there. It carries no roles.</li>
 *   <li>The ID token says who the person is: username, name, job title and department. No API
 *       accepts it, and it grants nothing.</li>
 * </ul>
 *
 * <p>{@code token_use} tells the two kinds of access token apart, so an API cannot take one for the
 * other.
 */
@Component
public class StaffTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    static final String ROLES_CLAIM = "roles";
    static final String ELIGIBLE_ROLES_CLAIM = "eligible_roles";
    static final String TOKEN_USE_CLAIM = "token_use";

    private final DirectoryApi directoryApi;
    private final GrantProjection grantProjection;

    public StaffTokenCustomizer(DirectoryApi directoryApi, GrantProjection grantProjection) {
        this.directoryApi = directoryApi;
        this.grantProjection = grantProjection;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        boolean isAccessToken = OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType());
        boolean isIdToken = OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue());
        boolean isForAService = AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType());

        if (isAccessToken && isForAService) {
            this.describeService(context);
        } else if (isAccessToken) {
            this.describeAccess(context);
        } else if (isIdToken) {
            this.describePerson(context);
        }
    }

    private void describeAccess(JwtEncodingContext context) {
        RequestedAccess access = requestedAccess(context);
        String resource = access.resource().orElseThrow(StaffTokenCustomizer::noSingleResource);
        UUID personId = UUID.fromString(context.getPrincipal().getName());
        List<UUID> groupIds = this.directoryApi.groupsOf(personId);
        List<String> eligibleRoles = this.grantProjection.findRoles(new RolesOfGroups(resource, groupIds));
        boolean holdsOnlyOneRole = eligibleRoles.size() == 1;
        List<String> activeRoles = access.actAs()
            .map(List::of)
            .orElse(holdsOnlyOneRole
                ? eligibleRoles
                : List.of()
            );

        context.getClaims()
            .subject(personId.toString())
            .audience(List.of(resource))
            .claim("azp", context.getRegisteredClient().getClientId())
            .claim(TOKEN_USE_CLAIM, "user")
            .claim(ROLES_CLAIM, activeRoles)
            .claim(ELIGIBLE_ROLES_CLAIM, eligibleRoles);
    }

    private void describeService(JwtEncodingContext context) {
        String clientId = context.getRegisteredClient().getClientId();
        String resource = requestedAccess(context).resource().orElseThrow(StaffTokenCustomizer::noSingleResource);
        List<String> granted = this.grantProjection.findScopes(new ScopesOfClient(clientId, resource));
        List<String> scopes = context.getAuthorizedScopes().stream()
            .filter(granted::contains)
            .sorted()
            .toList();

        context.getClaims()
            .subject(clientId)
            .audience(List.of(resource))
            .claim("azp", clientId)
            .claim(TOKEN_USE_CLAIM, "service")
            .claim("scope", String.join(" ", scopes));
    }

    private void describePerson(JwtEncodingContext context) {
        UUID personId = UUID.fromString(context.getPrincipal().getName());

        context.getClaims().claim("azp", context.getRegisteredClient().getClientId());
        this.directoryApi.findPeople(List.of(personId)).stream()
            .findFirst()
            .ifPresent(profile -> this.describe(context, profile));
    }

    private void describe(JwtEncodingContext context, PersonProfile profile) {
        context.getClaims()
            .subject(profile.id().toString())
            .claim("preferred_username", profile.username())
            .claim("name", profile.displayName())
            .claim("job_title", profile.jobTitle())
            .claim("department", profile.department());
    }

    /** A token request that names no API, or several, has no audience to give the token. */
    private static OAuth2AuthenticationException noSingleResource() {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE, "Name exactly one API", null));
    }

    private static RequestedAccess requestedAccess(JwtEncodingContext context) {
        Collection<String> clientResources = context.getRegisteredClient()
            .getClientSettings()
            .getSetting(RequestedAccess.RESOURCES_SETTING);
        Set<String> scopes = context.getAuthorizedScopes();

        return RequestedAccess.of(scopes, clientResources);
    }
}
