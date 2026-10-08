package lat.soulware.staff.directory.application.queries.people;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lat.soulware.staff.directory.application.queries.people.criteria.PeopleById;
import lat.soulware.staff.directory.application.queries.people.criteria.PeoplePage;
import lat.soulware.staff.directory.application.queries.people.projections.PersonProjection;
import lat.soulware.staff.directory.application.queries.people.results.PeopleLookupResult;
import lat.soulware.staff.directory.application.queries.people.results.PersonResult;
import lat.soulware.staff.directory.application.queries.people.results.PeoplePageResult;
import lat.soulware.staff.directory.domain.model.exceptions.MissingPersonIdException;
import lat.soulware.staff.directory.domain.model.exceptions.TooManyPeopleRequestedException;
import lat.soulware.staff.shared.application.queries.QueryService;
import lat.soulware.staff.shared.domain.model.exceptions.InvalidPageException;
import lat.soulware.staff.shared.domain.model.exceptions.InvalidSortDirectionException;
import lat.soulware.staff.shared.domain.model.exceptions.UnsortableColumnException;

/** Answers the reads other services make of the directory. */
@Service
@Transactional(readOnly = true)
public class PersonQueryService implements QueryService {

    private static final int MAX_LOOKUP_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 100;

    /** The columns a page of people sorts by, as the client names them. */
    private static final String SORTABLE = "username, displayName, jobTitle, department";
    private static final Pattern SORTABLE_COLUMN = Pattern.compile("^(username|displayName|jobTitle|department)$");

    private final PersonProjection personProjection;

    public PersonQueryService(PersonProjection personProjection) {
        this.personProjection = personProjection;
    }

    /**
     * @throws UnsortableColumnException     if the sort names no sortable column
     * @throws InvalidSortDirectionException if the direction is neither asc nor desc
     * @throws InvalidPageException          if the page is negative or the size out of range
     */
    public PeoplePageResult page(PeoplePage criteria) {
        boolean isSortable = criteria.sort() != null && SORTABLE_COLUMN.matcher(criteria.sort()).matches();
        if (!isSortable) {
            throw new UnsortableColumnException(criteria.sort(), SORTABLE);
        }
        boolean isADirection = "asc".equalsIgnoreCase(criteria.direction())
            || "desc".equalsIgnoreCase(criteria.direction());
        if (!isADirection) {
            throw new InvalidSortDirectionException(criteria.direction());
        }
        boolean isAPage = criteria.page() >= 0 && criteria.size() >= 1 && criteria.size() <= MAX_PAGE_SIZE;
        if (!isAPage) {
            throw new InvalidPageException(criteria.page(), criteria.size(), MAX_PAGE_SIZE);
        }

        return this.personProjection.findPage(criteria);
    }

    /**
     * Resolves up to {@value #MAX_LOOKUP_SIZE} GUIDs at once. A GUID nobody in the directory holds comes back
     * under {@code notFound} instead of failing the read.
     */
    public PeopleLookupResult lookup(PeopleById criteria) {
        if (criteria.ids().size() > MAX_LOOKUP_SIZE) {
            throw new TooManyPeopleRequestedException(MAX_LOOKUP_SIZE, criteria.ids().size());
        }
        if (criteria.ids().contains(null)) {
            throw new MissingPersonIdException();
        }

        Set<UUID> requested = new LinkedHashSet<>(criteria.ids());
        PeopleById distinct = new PeopleById(List.copyOf(requested));
        List<PersonResult> found = this.personProjection.findByIds(distinct);
        Set<UUID> foundIds = found.stream()
            .map(p -> p.id())
            .collect(Collectors.toSet());
        List<UUID> notFound = requested.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();

        return new PeopleLookupResult(found, notFound);
    }
}
