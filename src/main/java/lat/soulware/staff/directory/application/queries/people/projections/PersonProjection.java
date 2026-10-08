package lat.soulware.staff.directory.application.queries.people.projections;

import java.util.List;

import lat.soulware.staff.directory.application.queries.people.criteria.PeopleById;
import lat.soulware.staff.directory.application.queries.people.criteria.PeoplePage;
import lat.soulware.staff.directory.application.queries.people.results.PeoplePageResult;
import lat.soulware.staff.directory.application.queries.people.results.PersonResult;
import lat.soulware.staff.shared.application.queries.projections.Projection;

/** The people the directory holds. Read from the database now, and from LDAP once the real directory is in place. */
public interface PersonProjection extends Projection {

    /**
     * @param criteria the page to read
     * @return the page, empty when it lies past the last person
     */
    PeoplePageResult findPage(PeoplePage criteria);

    /**
     * @param criteria the GUIDs to resolve, distinct and non-null
     * @return the people holding them, in no particular order
     */
    List<PersonResult> findByIds(PeopleById criteria);
}
