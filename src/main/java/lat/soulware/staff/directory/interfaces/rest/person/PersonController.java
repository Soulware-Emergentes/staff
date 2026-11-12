package lat.soulware.staff.directory.interfaces.rest.person;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

import lat.soulware.staff.directory.application.queries.people.PersonQueryService;
import lat.soulware.staff.directory.interfaces.rest.person.mappers.PersonWireMapper;
import lat.soulware.staff.directory.interfaces.rest.person.requests.PeopleLookupRequest;
import lat.soulware.staff.directory.interfaces.rest.person.responses.PeopleLookupResponse;
import lat.soulware.staff.directory.interfaces.rest.person.responses.PeoplePageResponse;

/**
 * The directory's people, listed and looked up for other services. A lookup is by GUID whatever the
 * person's job, since what a person does in an application is not the directory's question.
 */
@RestController
@RequestMapping("/api/v3/people")
public class PersonController {

    private final PersonQueryService personQueryService;

    public PersonController(PersonQueryService personQueryService) {
        this.personQueryService = personQueryService;
    }

    @Operation(summary = "Returns a paginated set of people in the directory")
    @GetMapping
    public PeoplePageResponse page(
        @RequestParam(required = false) String jobTitle,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String displayName,
        @RequestParam(defaultValue = "displayName") String sort,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return PersonWireMapper.toPeoplePageResponse(this.personQueryService.page(
            PersonWireMapper.toPeoplePage(jobTitle, department, username, displayName, sort, direction, page, size)
        ));
    }

    /**
     * Resolves one or many directory GUIDs in a single request. GUIDs nobody holds come back under
     * {@code notFound} instead of failing the request.
     */
    @Operation(summary = "Returns the people holding the given ids, and the ids nobody holds")
    @PostMapping("/lookup")
    public PeopleLookupResponse lookup(@RequestBody PeopleLookupRequest request) {
        return PersonWireMapper.toPeopleLookupResponse(
            this.personQueryService.lookup(PersonWireMapper.toPeopleById(request))
        );
    }
}
