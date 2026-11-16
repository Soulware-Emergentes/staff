package lat.soulware.staff.directory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lat.soulware.staff.directory.publishedlanguage.PersonProfile;
import lat.soulware.staff.shared.ModuleApi;

/**
 * What the directory answers other modules, the questions LDAP answers: does this password open this
 * account, who are these people, and which groups is this person in. People and groups are named by
 * the objectGUIDs of their accounts.
 */
public interface DirectoryApi extends ModuleApi {

    /**
     * Checks credentials the way a bind does.
     *
     * @param username the account name typed
     * @param password the password typed
     * @return the objectGUID of the account they open, empty when they open none
     */
    Optional<UUID> authenticate(String username, String password);

    /**
     * @param ids the objectGUIDs of the people to describe
     * @return the profiles found, one per distinct GUID, in unspecified order
     */
    List<PersonProfile> findPeople(Collection<UUID> ids);

    /**
     * @param personId the objectGUID of a person's account
     * @return the objectGUIDs of the groups they belong to, empty when none or when nobody holds it
     */
    List<UUID> groupsOf(UUID personId);
}
