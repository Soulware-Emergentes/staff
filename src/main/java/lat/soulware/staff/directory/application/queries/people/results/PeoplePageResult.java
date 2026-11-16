package lat.soulware.staff.directory.application.queries.people.results;

import java.util.List;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * One page of people, ordered by display name.
 *
 * @param content       the people on the page
 * @param totalPages    how many pages the whole selection fills
 * @param totalElements how many people the whole selection holds
 * @param page          the page, counted from zero
 * @param size          how many people a page holds
 */
public record PeoplePageResult(
    List<PersonResult> content,
    int totalPages,
    long totalElements,
    int page,
    int size
) implements QueryResult {

    public PeoplePageResult {
        content = List.copyOf(content);
    }
}
