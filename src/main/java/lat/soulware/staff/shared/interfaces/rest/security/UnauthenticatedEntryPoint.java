package lat.soulware.staff.shared.interfaces.rest.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lat.soulware.staff.shared.domain.model.exceptions.UnauthenticatedException;

/**
 * Answers a request the token filter turned away: no bearer token, or one that is malformed,
 * expired, or issued by anyone but this server.
 *
 * <p>The filter runs before Spring MVC, so no controller advice sees what it rejects. This hands the
 * rejection to MVC's exception resolvers as an {@link UnauthenticatedException}, and
 * {@code DomainExceptionHandler} answers it with the same status and envelope as every other
 * authentication failure. The {@code WWW-Authenticate} header OAuth clients read the cause from is
 * set first, the way Spring's own bearer entry point sets it.
 */
@Component
public class UnauthenticatedEntryPoint implements AuthenticationEntryPoint {

    private final BearerTokenAuthenticationEntryPoint bearerChallenge = new BearerTokenAuthenticationEntryPoint();
    private final HandlerExceptionResolver resolver;

    public UnauthenticatedEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException rejection
    ) {
        this.bearerChallenge.commence(request, response, rejection);
        this.resolver.resolveException(request, response, null, new UnauthenticatedException());
    }
}
