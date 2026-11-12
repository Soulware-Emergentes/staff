package lat.soulware.staff.directory.infrastructure.persistence.person;

import java.util.Collection;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import lat.soulware.staff.directory.domain.model.aggregates.Person;
import lat.soulware.staff.directory.domain.model.exceptions.PersonNotFoundException;
import lat.soulware.staff.directory.domain.model.valueobjects.Department;
import lat.soulware.staff.directory.domain.model.valueobjects.DisplayName;
import lat.soulware.staff.directory.domain.model.valueobjects.JobTitle;
import lat.soulware.staff.directory.domain.model.valueobjects.PasswordHash;
import lat.soulware.staff.directory.domain.model.valueobjects.PersonId;
import lat.soulware.staff.directory.domain.model.valueobjects.Username;
import lat.soulware.staff.directory.domain.repositories.PersonRepository;
import lat.soulware.staff.directory.infrastructure.persistence.person.jpa.PersonEntity;
import lat.soulware.staff.directory.infrastructure.persistence.person.jpa.PersonJpaRepository;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;
import lat.soulware.staff.shared.infrastructure.persistence.adapter.JpaRepositoryToDomainRepositoryAdapter;

/** Stores people in the {@code people} table. */
@Repository
public class JpaPersonRepositoryAdapter
    extends JpaRepositoryToDomainRepositoryAdapter<Person, PersonId, PersonEntity, UUID>
    implements PersonRepository {

    private final PersonJpaRepository delegate;
    private final ApplicationEventPublisher publisher;

    public JpaPersonRepositoryAdapter(PersonJpaRepository delegate, ApplicationEventPublisher publisher) {
        this.delegate = delegate;
        this.publisher = publisher;
    }

    @Override
    protected PersonJpaRepository delegate() {
        return this.delegate;
    }

    @Override
    protected ApplicationEventPublisher publisher() {
        return this.publisher;
    }

    @Override
    protected PersonEntity toEntity(Person aggregate) {
        boolean canLogIn = aggregate.getPasswordHash() != null;
        String passwordHash = canLogIn
            ? aggregate.getPasswordHash().value()
            : null;

        return new PersonEntity(
            aggregate.getId().value(),
            aggregate.getUsername().value(),
            aggregate.getDisplayName().value(),
            aggregate.getJobTitle().value(),
            aggregate.getDepartment().value(),
            passwordHash
        );
    }

    @Override
    protected Person toAggregate(PersonEntity entity) {
        boolean canLogIn = entity.getPasswordHash() != null;
        PasswordHash passwordHash = canLogIn
            ? new PasswordHash(entity.getPasswordHash())
            : null;

        return Person.reconstitute(
            new PersonId(entity.getId()),
            new Username(entity.getUsername()),
            new DisplayName(entity.getDisplayName()),
            new JobTitle(entity.getJobTitle()),
            new Department(entity.getDepartment()),
            passwordHash
        );
    }

    @Override
    protected UUID keyOf(PersonEntity entity) {
        return entity.getId();
    }

    @Override
    protected EntityNotFoundException notFound(Collection<PersonId> missing) {
        return new PersonNotFoundException(missing);
    }
}
