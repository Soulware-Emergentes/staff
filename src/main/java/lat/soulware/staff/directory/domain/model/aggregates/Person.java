package lat.soulware.staff.directory.domain.model.aggregates;

import lat.soulware.staff.directory.domain.model.exceptions.MissingDepartmentException;
import lat.soulware.staff.directory.domain.model.exceptions.MissingDisplayNameException;
import lat.soulware.staff.directory.domain.model.exceptions.MissingJobTitleException;
import lat.soulware.staff.directory.domain.model.exceptions.MissingUsernameException;
import lat.soulware.staff.directory.domain.model.valueobjects.Department;
import lat.soulware.staff.directory.domain.model.valueobjects.DisplayName;
import lat.soulware.staff.directory.domain.model.valueobjects.JobTitle;
import lat.soulware.staff.directory.domain.model.valueobjects.PasswordHash;
import lat.soulware.staff.directory.domain.model.valueobjects.PersonId;
import lat.soulware.staff.directory.domain.model.valueobjects.Username;
import lat.soulware.staff.shared.domain.model.aggregates.AggregateRoot;
import lat.soulware.staff.shared.domain.model.aggregates.PackagedAs;

/**
 * A person in the organization's directory, identified by the objectGUID of their account. Their job
 * title and department say who they are in the organization, and grant nothing in any application.
 * A person without a password hash is a directory entry that cannot log in.
 */
@PackagedAs("person")
public class Person extends AggregateRoot<PersonId> {
    private final PersonId id;
    private final Username username;
    private final DisplayName displayName;
    private final JobTitle jobTitle;
    private final Department department;
    private final PasswordHash passwordHash;

    private Person(
        PersonId id,
        Username username,
        DisplayName displayName,
        JobTitle jobTitle,
        Department department,
        PasswordHash passwordHash
    ) {
        if (username == null) {
            throw new MissingUsernameException();
        }
        if (displayName == null) {
            throw new MissingDisplayNameException();
        }
        if (jobTitle == null) {
            throw new MissingJobTitleException();
        }
        if (department == null) {
            throw new MissingDepartmentException();
        }
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.jobTitle = jobTitle;
        this.department = department;
        this.passwordHash = passwordHash;
    }

    /**
     * Rebuilds a person as the directory holds them.
     *
     * @param id           the objectGUID of their account
     * @param username     the account name they log in with
     * @param displayName  their name as shown to others
     * @param jobTitle     what they do in the organization
     * @param department   where in the organization they do it
     * @param passwordHash their password hash, null when they cannot log in
     * @return the person as stored
     */
    public static Person reconstitute(
        PersonId id,
        Username username,
        DisplayName displayName,
        JobTitle jobTitle,
        Department department,
        PasswordHash passwordHash
    ) {
        return new Person(id, username, displayName, jobTitle, department, passwordHash);
    }

    @Override
    public PersonId getId() {
        return this.id;
    }

    public Username getUsername() {
        return this.username;
    }

    public DisplayName getDisplayName() {
        return this.displayName;
    }

    public JobTitle getJobTitle() {
        return this.jobTitle;
    }

    public Department getDepartment() {
        return this.department;
    }

    /** @return their password hash, null when they cannot log in */
    public PasswordHash getPasswordHash() {
        return this.passwordHash;
    }
}
