package lat.soulware.staff.directory.domain.repositories;

import lat.soulware.staff.directory.domain.model.aggregates.Person;
import lat.soulware.staff.directory.domain.model.valueobjects.PersonId;
import lat.soulware.staff.shared.domain.repositories.DomainRepository;

/** Stores {@link Person} aggregates. */
public interface PersonRepository extends DomainRepository<Person, PersonId> {
}
