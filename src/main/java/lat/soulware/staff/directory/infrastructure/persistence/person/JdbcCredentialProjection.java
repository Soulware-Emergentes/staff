package lat.soulware.staff.directory.infrastructure.persistence.person;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.application.queries.accounts.criteria.Credentials;
import lat.soulware.staff.directory.application.queries.accounts.projections.CredentialProjection;

/**
 * Checks a password against the BCrypt hash the {@code people} table holds. This is the class an LDAP
 * bind replaces once the real directory is in place.
 */
@Component
public class JdbcCredentialProjection implements CredentialProjection {

    private static final String FIND_ACCOUNT = """
        SELECT id, password_hash
        FROM people
        WHERE username = :username AND password_hash IS NOT NULL""";

    private final NamedParameterJdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public JdbcCredentialProjection(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UUID> findAuthenticated(Credentials criteria) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("username", criteria.username());

        return this.jdbc.query(FIND_ACCOUNT, parameters, (row, rowNumber) -> new Account(
                row.getObject("id", UUID.class),
                row.getString("password_hash")
            ))
            .stream()
            .findFirst()
            .filter(account -> this.passwordEncoder.matches(criteria.password(), account.passwordHash()))
            .map(account -> account.id());
    }

    private record Account(UUID id, String passwordHash) {
    }
}
