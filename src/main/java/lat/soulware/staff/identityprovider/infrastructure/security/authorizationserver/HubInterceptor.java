package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lat.soulware.staff.directory.DirectoryApi;
import lat.soulware.staff.directory.publishedlanguage.PersonProfile;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientsOfGroups;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;
import lat.soulware.staff.identityprovider.application.queries.clients.results.LaunchableClientResult;

/**
 * Fills the hub for the logged-in person: who they are, as the directory describes them, and the
 * applications they can open, which are the public clients of the APIs their groups hold a role on. A
 * deployment the person holds no role on is left out, since its login would refuse them.
 *
 * <p>Applications are grouped by environment, production first since it is the one people use for
 * real. Only applications outside production carry their environment's name, the way the login page
 * names it.
 */
@Component
public class HubInterceptor implements HandlerInterceptor {

    static final String PERSON_ATTRIBUTE = "person";
    static final String ENVIRONMENTS_ATTRIBUTE = "environments";
    static final String APPLICATION_COUNT_ATTRIBUTE = "applicationCount";

    private static final String ENVIRONMENT_MESSAGE_PREFIX = "environment.";
    private static final String PRODUCTION = "prod";
    private static final int MONOGRAM_LENGTH = 3;
    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    private final DirectoryApi directoryApi;
    private final ClientProjection clientProjection;
    private final MessageSource messageSource;

    public HubInterceptor(DirectoryApi directoryApi, ClientProjection clientProjection, MessageSource messageSource) {
        this.directoryApi = directoryApi;
        this.clientProjection = clientProjection;
        this.messageSource = messageSource;
    }

    @Override
    public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (modelAndView == null || !TRUST_RESOLVER.isAuthenticated(authentication)) {
            return;
        }
        UUID personId = UUID.fromString(authentication.getName());

        this.directoryApi.findPeople(List.of(personId)).stream()
            .findFirst()
            .map(HubInterceptor::personOf)
            .ifPresent(person -> modelAndView.addObject(PERSON_ATTRIBUTE, person));

        List<EnvironmentApplications> environments = this.launchableBy(personId, RequestContextUtils.getLocale(request));
        modelAndView.addObject(ENVIRONMENTS_ATTRIBUTE, environments);
        modelAndView.addObject(
            APPLICATION_COUNT_ATTRIBUTE,
            environments.stream().mapToInt(environment -> environment.applications().size()).sum()
        );
    }

    /**
     * @param personId the objectGUID of the person
     * @param locale   the language environments are named in
     * @return the applications the person can open, by environment, production first
     */
    List<EnvironmentApplications> launchableBy(UUID personId, Locale locale) {
        List<UUID> groupIds = this.directoryApi.groupsOf(personId);
        List<LaunchableClientResult> clients = this.clientProjection.findLaunchable(new ClientsOfGroups(groupIds));

        return this.byEnvironment(clients, locale);
    }

    private List<EnvironmentApplications> byEnvironment(List<LaunchableClientResult> clients, Locale locale) {
        Map<String, List<LaunchableClientResult>> grouped = new LinkedHashMap<>();
        clients.forEach(client -> grouped.computeIfAbsent(client.environment(), code -> new ArrayList<>()).add(client));

        return grouped.values().stream()
            .sorted(Comparator.comparing(inEnvironment -> !isProduction(inEnvironment.getFirst())))
            .map(inEnvironment -> {
                LaunchableClientResult first = inEnvironment.getFirst();
                String environmentName = this.environmentName(first, locale);
                return new EnvironmentApplications(
                    first.environment(),
                    environmentName,
                    isProduction(first),
                    inEnvironment.stream().map(client -> applicationOf(client, environmentName)).toList()
                );
            })
            .toList();
    }

    private String environmentName(LaunchableClientResult client, Locale locale) {
        return this.messageSource.getMessage(
            ENVIRONMENT_MESSAGE_PREFIX + client.environment(),
            null,
            client.environmentName(),
            locale
        );
    }

    private static boolean isProduction(LaunchableClientResult client) {
        return PRODUCTION.equals(client.environment());
    }

    private static Application applicationOf(LaunchableClientResult client, String environmentName) {
        return new Application(
            client.clientId(),
            client.applicationName(),
            monogramOf(client.applicationName()),
            client.homeUri(),
            URI.create(client.homeUri()).getAuthority(),
            client.environment(),
            isProduction(client) ? null : environmentName
        );
    }

    private static Person personOf(PersonProfile profile) {
        List<String> words = wordsOf(profile.displayName());
        String initials = words.size() == 1
            ? words.getFirst().substring(0, 1)
            : words.getFirst().substring(0, 1) + words.getLast().substring(0, 1);

        return new Person(profile.displayName(), words.getFirst(), profile.jobTitle(), initials.toUpperCase(Locale.ROOT));
    }

    /** SGT stays SGT and Beneficiarios becomes BEN; a name of several words is shown by their initials. */
    private static String monogramOf(String name) {
        List<String> words = wordsOf(name);
        String monogram = words.size() == 1
            ? words.getFirst().substring(0, Math.min(MONOGRAM_LENGTH, words.getFirst().length()))
            : words.stream().limit(MONOGRAM_LENGTH).map(word -> word.substring(0, 1)).collect(Collectors.joining());

        return monogram.toUpperCase(Locale.ROOT);
    }

    private static List<String> wordsOf(String text) {
        return Arrays.stream(text.trim().split("\\s+")).filter(word -> !word.isEmpty()).toList();
    }

    /**
     * The logged-in person, as the hub greets them.
     *
     * @param displayName their name as shown to others
     * @param firstName   the first word of it, which the greeting uses
     * @param jobTitle    what they do in the organization
     * @param initials    what their avatar shows
     */
    public record Person(String displayName, String firstName, String jobTitle, String initials) {
    }

    /**
     * The applications one environment holds for the person.
     *
     * @param code         the environment's code, such as {@code dev}
     * @param name         its name, in the page's language
     * @param production   whether it is the one people use for real
     * @param applications its applications, by name
     */
    public record EnvironmentApplications(String code, String name, boolean production, List<Application> applications) {
    }

    /**
     * An application the person can open.
     *
     * @param clientId        the public client it opens as
     * @param name            the name people know its system by
     * @param monogram        the letters its tile shows
     * @param homeUri         where it opens
     * @param host            the host and port of that address, which the card shows
     * @param environment     the code of the environment it is deployed to
     * @param environmentName that environment's name, null in production, which goes unnamed
     */
    public record Application(
        String clientId,
        String name,
        String monogram,
        String homeUri,
        String host,
        String environment,
        String environmentName
    ) {
    }
}
