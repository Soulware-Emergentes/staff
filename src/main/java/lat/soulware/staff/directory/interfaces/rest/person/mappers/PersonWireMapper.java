package lat.soulware.staff.directory.interfaces.rest.person.mappers;

import java.util.List;
import java.util.UUID;

import lat.soulware.staff.directory.application.queries.people.criteria.PeopleById;
import lat.soulware.staff.directory.application.queries.people.criteria.PeoplePage;
import lat.soulware.staff.directory.application.queries.people.results.PeopleLookupResult;
import lat.soulware.staff.directory.application.queries.people.results.PeoplePageResult;
import lat.soulware.staff.directory.application.queries.people.results.PersonResult;
import lat.soulware.staff.directory.interfaces.rest.person.requests.PeopleLookupRequest;
import lat.soulware.staff.directory.interfaces.rest.person.responses.PeopleLookupResponse;
import lat.soulware.staff.directory.interfaces.rest.person.responses.PeoplePageResponse;
import lat.soulware.staff.directory.interfaces.rest.person.responses.PersonResponse;
import lat.soulware.staff.shared.interfaces.rest.mappers.WireMapper;

/** Converts what {@code PersonController} receives and answers. */
public final class PersonWireMapper implements WireMapper {

    private PersonWireMapper() {
    }

    public static PeoplePage toPeoplePage(
        String jobTitle,
        String department,
        String username,
        String displayName,
        String sort,
        String direction,
        int page,
        int size
    ) {
        return new PeoplePage(jobTitle, department, username, displayName, sort, direction, page, size);
    }

    public static PeopleById toPeopleById(PeopleLookupRequest request) {
        List<UUID> ids = request.ids() == null
            ? List.of()
            : request.ids();

        return new PeopleById(ids);
    }

    public static PeoplePageResponse toPeoplePageResponse(PeoplePageResult result) {
        return new PeoplePageResponse(
            toPersonResponses(result.content()),
            result.totalPages(),
            result.totalElements(),
            result.page(),
            result.size()
        );
    }

    public static PeopleLookupResponse toPeopleLookupResponse(PeopleLookupResult result) {
        return new PeopleLookupResponse(toPersonResponses(result.found()), result.notFound());
    }

    private static List<PersonResponse> toPersonResponses(List<PersonResult> results) {
        return results.stream()
            .map(person -> new PersonResponse(
                person.id(),
                person.username(),
                person.displayName(),
                person.jobTitle(),
                person.department()
            ))
            .toList();
    }
}
