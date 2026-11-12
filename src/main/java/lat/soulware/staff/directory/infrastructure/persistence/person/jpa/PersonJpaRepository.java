package lat.soulware.staff.directory.infrastructure.persistence.person.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** The Spring Data delegate behind {@code JpaPersonRepositoryAdapter}. */
public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {
}
