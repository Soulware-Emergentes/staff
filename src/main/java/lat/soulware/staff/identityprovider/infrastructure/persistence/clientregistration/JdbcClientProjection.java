package lat.soulware.staff.identityprovider.infrastructure.persistence.clientregistration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientById;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;

/** Reads a client registration with its redirect URIs, the APIs it may call, and what it may ask for there. */
@Component
public class JdbcClientProjection implements ClientProjection {

    private static final String FIND_BY_CLIENT_ID = """
        SELECT id, client_id, kind, secret_hash
        FROM client_registrations
        WHERE client_id = :key""";

    private static final String FIND_BY_ID = """
        SELECT id, client_id, kind, secret_hash
        FROM client_registrations
        WHERE id = :key""";

    private static final String FIND_REDIRECT_URIS = """
        SELECT uri
        FROM client_redirect_uris
        WHERE client_registration_id = :id AND kind = :kind
        ORDER BY uri""";

    private static final String FIND_RESOURCES = """
        SELECT r.identifier
        FROM client_resources cr
        JOIN api_resources r ON r.id = cr.resource_id
        WHERE cr.client_registration_id = :id
        ORDER BY r.identifier""";

    private static final String FIND_ROLES = """
        SELECT DISTINCT ar.value
        FROM client_resources cr
        JOIN api_roles ar ON ar.resource_id = cr.resource_id
        WHERE cr.client_registration_id = :id
        ORDER BY ar.value""";

    private static final String FIND_GRANTED_SCOPES = """
        SELECT DISTINCT s.value
        FROM scope_grants g
        JOIN api_scopes s ON s.id = g.scope_id
        WHERE g.client_registration_id = :id
        ORDER BY s.value""";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcClientProjection(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ClientResult> findByClientId(ClientByClientId criteria) {
        return this.find(FIND_BY_CLIENT_ID, criteria.clientId());
    }

    @Override
    public Optional<ClientResult> findById(ClientById criteria) {
        return this.find(FIND_BY_ID, criteria.id());
    }

    private Optional<ClientResult> find(String query, Object key) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("key", key);

        return this.jdbc.query(query, parameters, (row, rowNumber) -> new Registration(
                row.getObject("id", UUID.class),
                row.getString("client_id"),
                row.getString("kind"),
                row.getString("secret_hash")
            ))
            .stream()
            .findFirst()
            .map(this::complete);
    }

    private ClientResult complete(Registration registration) {
        MapSqlParameterSource byId = new MapSqlParameterSource("id", registration.id());

        return new ClientResult(
            registration.id(),
            registration.clientId(),
            registration.kind(),
            registration.secretHash(),
            this.redirectUris(registration.id(), "LOGIN"),
            this.redirectUris(registration.id(), "LOGOUT"),
            this.jdbc.queryForList(FIND_RESOURCES, byId, String.class),
            this.jdbc.queryForList(FIND_ROLES, byId, String.class),
            this.jdbc.queryForList(FIND_GRANTED_SCOPES, byId, String.class)
        );
    }

    private List<String> redirectUris(UUID id, String kind) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("kind", kind);

        return this.jdbc.queryForList(FIND_REDIRECT_URIS, parameters, String.class);
    }

    private record Registration(UUID id, String clientId, String kind, String secretHash) {
    }
}
