package lat.soulware.staff.identityprovider.application.queries.clients.results;

import lat.soulware.staff.shared.application.queries.results.QueryResult;

/**
 * An application a person can be logged into, as the hub lists it.
 *
 * @param clientId        the application's public name
 * @param applicationName the name people know its system by, such as SGT
 * @param environment     the code of the environment it is deployed to, such as {@code dev}
 * @param environmentName that environment's name, such as Development
 * @param homeUri         where the hub opens it
 */
public record LaunchableClientResult(
    String clientId,
    String applicationName,
    String environment,
    String environmentName,
    String homeUri
) implements QueryResult {
}
