package lat.soulware.staff.directory.infrastructure.persistence.person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lat.soulware.staff.directory.application.queries.people.criteria.PeopleById;
import lat.soulware.staff.directory.application.queries.people.criteria.PeoplePage;
import lat.soulware.staff.directory.application.queries.people.projections.PersonProjection;
import lat.soulware.staff.directory.application.queries.people.results.PeoplePageResult;
import lat.soulware.staff.directory.application.queries.people.results.PersonResult;

/** Reads people from the {@code people} table, a page at a time or by GUID. LDAP searches replace it. */
@Component
public class JdbcPersonProjection implements PersonProjection {

    private static final String MATCHING = """
        FROM people
        WHERE (CAST(:jobTitle AS VARCHAR) IS NULL OR lower(job_title) LIKE '%' || lower(:jobTitle) || '%')
            AND (CAST(:department AS VARCHAR) IS NULL OR lower(department) LIKE '%' || lower(:department) || '%')
            AND (CAST(:username AS VARCHAR) IS NULL OR lower(username) LIKE '%' || lower(:username) || '%')
            AND (CAST(:displayName AS VARCHAR) IS NULL OR lower(display_name) LIKE '%' || lower(:displayName) || '%')""";

    private static final String COUNT = "SELECT count(*) " + MATCHING;

    private static final String FIND_PAGE = """
        SELECT id, username, display_name, job_title, department
        %s
        ORDER BY %s %s, id
        LIMIT :size OFFSET :offset""";

    /** The column each sortable name orders by. */
    private static final Map<String, String> ORDER_BY = Map.of(
        "username", "username",
        "displayName", "display_name",
        "jobTitle", "job_title",
        "department", "department"
    );

    private static final String FIND_BY_IDS = """
        SELECT id, username, display_name, job_title, department
        FROM people
        WHERE id IN (:ids)""";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPersonProjection(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PeoplePageResult findPage(PeoplePage criteria) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("jobTitle", blankAsNull(criteria.jobTitle()))
            .addValue("department", blankAsNull(criteria.department()))
            .addValue("username", blankAsNull(criteria.username()))
            .addValue("displayName", blankAsNull(criteria.displayName()))
            .addValue("size", criteria.size())
            .addValue("offset", (long) criteria.page() * criteria.size());

        Long total = this.jdbc.queryForObject(COUNT, parameters, Long.class);
        long totalElements = total == null
            ? 0
            : total;
        int totalPages = (int) Math.ceilDiv(totalElements, criteria.size());
        boolean isDescending = "desc".equalsIgnoreCase(criteria.direction());
        String direction = isDescending ? "DESC" : "ASC";
        String findPage = FIND_PAGE.formatted(MATCHING, ORDER_BY.get(criteria.sort()), direction);
        List<PersonResult> content = this.jdbc.query(findPage, parameters, JdbcPersonProjection::toResult);

        return new PeoplePageResult(content, totalPages, totalElements, criteria.page(), criteria.size());
    }

    @Override
    public List<PersonResult> findByIds(PeopleById criteria) {
        if (criteria.ids().isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", criteria.ids());

        return this.jdbc.query(FIND_BY_IDS, parameters, JdbcPersonProjection::toResult);
    }

    private static PersonResult toResult(ResultSet row, int rowNumber) throws SQLException {
        return new PersonResult(
            row.getObject("id", UUID.class),
            row.getString("username"),
            row.getString("display_name"),
            row.getString("job_title"),
            row.getString("department")
        );
    }

    private static String blankAsNull(String text) {
        boolean isBlank = text == null || text.isBlank();
        return isBlank
            ? null
            : text;
    }
}
