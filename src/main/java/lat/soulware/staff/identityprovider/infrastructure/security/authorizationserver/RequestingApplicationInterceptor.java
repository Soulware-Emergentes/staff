package lat.soulware.staff.identityprovider.infrastructure.security.authorizationserver;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lat.soulware.staff.identityprovider.application.queries.clients.criteria.ClientByClientId;
import lat.soulware.staff.identityprovider.application.queries.clients.projections.ClientProjection;
import lat.soulware.staff.identityprovider.application.queries.clients.results.ClientResult;

/**
 * Names, on the login page, the application whose login sent the browser there and, outside
 * production, the environment it runs in. Both come from the application's registration, found by the client id of the
 * authorization request Spring Security saved, never from anything the request itself says, so a
 * link cannot make the page claim to sign in to something else. Opened without the marker
 * {@link ContinuedLogin} puts on a login an application asked for, the page names nothing, even when
 * the session holds a request left over from an abandoned login.
 */
@Component
public class RequestingApplicationInterceptor implements HandlerInterceptor {

    static final String APPLICATION_ATTRIBUTE = "requestingApplication";
    static final String ENVIRONMENT_ATTRIBUTE = "requestingEnvironment";

    private static final String CLIENT_ID_PARAMETER = "client_id";
    private static final String ENVIRONMENT_MESSAGE_PREFIX = "environment.";

    /** The environment people use for real, which the page leaves unnamed. */
    private static final String PRODUCTION = "prod";

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final ClientProjection clientProjection;
    private final MessageSource messageSource;

    public RequestingApplicationInterceptor(ClientProjection clientProjection, MessageSource messageSource) {
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
        if (modelAndView == null || !ContinuedLogin.isContinued(request)) {
            return;
        }

        this.requestingClient(request, response).ifPresent(client -> {
            modelAndView.addObject(APPLICATION_ATTRIBUTE, client.applicationName());
            if (PRODUCTION.equals(client.environment())) {
                return;
            }
            modelAndView.addObject(ENVIRONMENT_ATTRIBUTE, this.messageSource.getMessage(
                ENVIRONMENT_MESSAGE_PREFIX + client.environment(),
                null,
                client.environmentName(),
                RequestContextUtils.getLocale(request)
            ));
        });
    }

    private Optional<ClientResult> requestingClient(HttpServletRequest request, HttpServletResponse response) {
        return Optional.ofNullable(this.requestCache.getRequest(request, response))
            .map(savedRequest -> savedRequest.getParameterMap())
            .map(parameters -> parameters.get(CLIENT_ID_PARAMETER))
            .filter(values -> values.length == 1)
            .flatMap(values -> this.clientProjection.findByClientId(new ClientByClientId(values[0])));
    }
}
