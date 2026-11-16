package lat.soulware.staff.directory.application.queries.accounts.results;

import java.util.UUID;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * The outcome of checking credentials against the directory, the way a bind answers.
 *
 * @param authenticated whether the credentials match an account that can log in
 * @param personId      the objectGUID of that account, null when not authenticated
 */
public record AuthenticationResult(boolean authenticated, UUID personId) implements QueryResult {
}
