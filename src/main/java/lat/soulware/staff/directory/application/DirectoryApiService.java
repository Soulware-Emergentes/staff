package lat.soulware.staff.directory.application;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lat.soulware.staff.directory.DirectoryApi;
import lat.soulware.staff.directory.application.queries.accounts.AccountQueryService;
import lat.soulware.staff.directory.application.queries.accounts.criteria.Credentials;
import lat.soulware.staff.directory.application.queries.accounts.results.AuthenticationResult;
import lat.soulware.staff.directory.application.queries.groups.GroupQueryService;
import lat.soulware.staff.directory.application.queries.groups.criteria.GroupsOfPerson;
import lat.soulware.staff.directory.application.queries.people.PersonQueryService;
import lat.soulware.staff.directory.application.queries.people.criteria.PeopleById;
import lat.soulware.staff.directory.publishedlanguage.PersonProfile;

/** Answers {@link DirectoryApi} through the module's query services. */
@Service
public class DirectoryApiService implements DirectoryApi {

    private final AccountQueryService accountQueryService;
    private final PersonQueryService personQueryService;
    private final GroupQueryService groupQueryService;

    public DirectoryApiService(
        AccountQueryService accountQueryService,
        PersonQueryService personQueryService,
        GroupQueryService groupQueryService
    ) {
        this.accountQueryService = accountQueryService;
        this.personQueryService = personQueryService;
        this.groupQueryService = groupQueryService;
    }

    @Override
    public Optional<UUID> authenticate(String username, String password) {
        AuthenticationResult result = this.accountQueryService.authenticate(new Credentials(username, password));

        return result.authenticated()
            ? Optional.of(result.personId())
            : Optional.empty();
    }

    @Override
    public List<PersonProfile> findPeople(Collection<UUID> ids) {
        return this.personQueryService.lookup(new PeopleById(List.copyOf(ids))).found().stream()
            .map(person -> new PersonProfile(
                person.id(),
                person.username(),
                person.displayName(),
                person.jobTitle(),
                person.department()
            ))
            .toList();
    }

    @Override
    public List<UUID> groupsOf(UUID personId) {
        return this.groupQueryService.groupsOf(new GroupsOfPerson(personId)).groupIds();
    }
}
