package lat.soulware.staff.directory.infrastructure.persistence.person;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.application.queries.groups.criteria.GroupsOfPerson;
import lat.soulware.staff.directory.application.queries.groups.projections.GroupMembershipProjection;

/** Reads a person's groups from {@code group_members}. LDAP's memberOf replaces it. */
@Component
public class JdbcGroupMembershipProjection implements GroupMembershipProjection {

    private static final String FIND_GROUP_IDS = """
        SELECT group_id
        FROM group_members
        WHERE person_id = :personId
        ORDER BY group_id""";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcGroupMembershipProjection(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UUID> findGroupIds(GroupsOfPerson criteria) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("personId", criteria.personId());

        return this.jdbc.query(FIND_GROUP_IDS, parameters, (row, rowNumber) -> row.getObject("group_id", UUID.class));
    }
}
