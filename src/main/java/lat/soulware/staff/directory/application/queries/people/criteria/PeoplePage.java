package lat.soulware.staff.directory.application.queries.people.criteria;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * One page of the directory's people, optionally narrowed. Every text filter matches when the field
 * contains it, ignoring case; null or blank matches everyone.
 *
 * @param jobTitle    text the job title contains
 * @param department  text the department contains
 * @param username    text the username contains
 * @param displayName text the display name contains
 * @param sort        the column to order by: {@code username}, {@code displayName}, {@code jobTitle}
 *                    or {@code department}
 * @param direction   {@code asc} or {@code desc}
 * @param page        the page, counted from zero
 * @param size        how many people a page holds
 */
public record PeoplePage(
    String jobTitle,
    String department,
    String username,
    String displayName,
    String sort,
    String direction,
    int page,
    int size
) implements QueryCriteria {
}
