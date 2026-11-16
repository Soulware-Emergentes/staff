package lat.soulware.staff.directory.infrastructure.persistence.person.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** The row storing a person. */
@Entity
@Table(name = "people")
public class PersonEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String department;

    @Column(name = "password_hash")
    private String passwordHash;

    /** For JPA. */
    protected PersonEntity() {
    }

    public PersonEntity(
        UUID id,
        String username,
        String displayName,
        String jobTitle,
        String department,
        String passwordHash
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.jobTitle = jobTitle;
        this.department = department;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getJobTitle() {
        return this.jobTitle;
    }

    public String getDepartment() {
        return this.department;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }
}
