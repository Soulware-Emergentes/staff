package lat.soulware.staff.directory.application.queries.accounts.criteria;

import lat.soulware.staff.shared.application.queries.criteria.QueryCriteria;

/**
 * What a person typed to log in.
 *
 * @param username the account name
 * @param password the password, in clear, never stored
 */
public record Credentials(String username, String password) implements QueryCriteria {

    @Override
    public String toString() {
        return "Credentials[username=" + this.username + ", password=<hidden>]";
    }
}
