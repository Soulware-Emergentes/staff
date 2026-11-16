package lat.soulware.staff.directory.interfaces.rest.person.responses;

import java.util.List;

/**
 * One page of people, ordered by display name.
 *
 * @param content       the people on the page
 * @param totalPages    how many pages the whole selection fills
 * @param totalElements how many people the whole selection holds
 * @param actualPage    the page, counted from zero
 * @param pageSize      how many people a page holds
 */
public record PeoplePageResponse(
    List<PersonResponse> content,
    int totalPages,
    long totalElements,
    int actualPage,
    int pageSize
) {
}
