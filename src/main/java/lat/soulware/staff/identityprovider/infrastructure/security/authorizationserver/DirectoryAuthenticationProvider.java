package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.DirectoryApi;

/**
 * Checks the login form's credentials against the directory, the way an identity provider binds to
 * LDAP. The logged-in principal is named by the person's objectGUID, which every token then carries
 * as its subject. What the person may do is decided per application, so the session grants nothing.
 *
 * <p>It records that a password was checked and when, the way Spring's own password provider does,
 * since an ID token states the time its person logged in.
 */
@Component
public class DirectoryAuthenticationProvider implements AuthenticationProvider {

    private static final String PERSON_AUTHORITY = "ROLE_PERSON";

    private final DirectoryApi directoryApi;

    public DirectoryAuthenticationProvider(DirectoryApi directoryApi) {
        this.directoryApi = directoryApi;
    }

    @Override
    public Authentication authenticate(Authentication login) {
        String password = String.valueOf(login.getCredentials());

        return this.directoryApi.authenticate(login.getName(), password)
            .map(personId -> UsernamePasswordAuthenticationToken.authenticated(
                personId.toString(),
                null,
                List.of(
                    new SimpleGrantedAuthority(PERSON_AUTHORITY),
                    FactorGrantedAuthority.withAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                        .issuedAt(Instant.now())
                        .build()
                )
            ))
            .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
