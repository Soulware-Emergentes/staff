package lat.soulware.staff.identityprovider.infrastructure.persistence.apiresource;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lat.soulware.staff.identityprovider.application.queries.grants.criteria.RolesOfGroups;
import lat.soulware.staff.identityprovider.application.queries.grants.criteria.ScopesOfClient;
import lat.soulware.staff.identityprovider.application.queries.grants.projections.GrantProjection;

/**
 * Reads role assignments and scope grants. Groups are named by the directory's objectGUIDs and carry
 * no foreign key, since the directory may live elsewhere.
 */
@Component
public class JdbcGrantProjection implements GrantProjection {

    private static final String FIND_ROLES = """
        SELECT DISTINCT ar.value
        FROM role_assignments a
        JOIN api_roles ar ON ar.id = a.role_id
        JOIN api_resources r ON r.id = ar.resource_id
        WHERE r.identifier = :resource AND a.group_id IN (:groupIds)
        ORDER BY ar.value""";

    private static final String FIND_SCOPES = """
        SELECT DISTINCT s.value
        FROM scope_grants g
        JOIN client_registrations c ON c.id = g.client_registration_id
        JOIN api_scopes s ON s.id = g.scope_id
        JOIN api_resources r ON r.id = s.resource_id
        WHERE c.client_id = :clientId AND r.identifier = :resource
        ORDER BY s.value""";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcGrantProjection(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<String> findRoles(RolesOfGroups criteria) {
        if (criteria.groupIds().isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("resource", criteria.resource())
            .addValue("groupIds", criteria.groupIds());

        return this.jdbc.queryForList(FIND_ROLES, parameters, String.class);
    }

    @Override
    public List<String> findScopes(ScopesOfClient criteria) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("clientId", criteria.clientId())
            .addValue("resource", criteria.resource());

        return this.jdbc.queryForList(FIND_SCOPES, parameters, String.class);
    }
}
