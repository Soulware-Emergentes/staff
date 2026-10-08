package lat.soulware.staff.shared.interfaces.rest.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lat.soulware.staff.shared.infrastructure.exceptions.InfrastructureException;
import lat.soulware.staff.shared.interfaces.rest.errors.responses.ErrorResponse;
import lat.soulware.staff.shared.interfaces.rest.logging.RequestIdFilter;

/**
 * States what a technical failure means over HTTP. A port declares no failure of its own, so what
 * an adapter could not supply arrives here unchecked, and this is the only place that turns it into
 * a status code and a response body.
 *
 * <p>The caller is told that the request did not complete, under the identifier
 * {@link RequestIdFilter} assigned the request. The technology, the cause, and the stack go to the
 * log, where they are the operator's to read, under that same identifier.
 *
 * <p>Declared at lowest precedence, behind {@link DomainExceptionHandler}, so a failure the domain
 * named is answered as that kind rather than as a technical one.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class InfrastructureExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureExceptionHandler.class);

    /** Reported for a failure nobody anticipated, which is every unchecked type but the one above. */
    private static final String UNEXPECTED_KEY = "error.shared.unexpected";

    private final MessageSource messages;

    /**
     * @param messages resolves message keys against the bundles and the request's locale
     */
    public InfrastructureExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Answers a failure an adapter raised deliberately, under a status that says the capability may
     * be there on a later attempt.
     *
     * @param failure the failure to report
     * @return the envelope, carrying the bundle's wording rather than the adapter's detail
     */
    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ErrorResponse> handle(InfrastructureException failure) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        LOG.error("Infrastructure failure, answering with {}", status, failure);

        ErrorResponse body = ErrorResponse.of(
            status,
            this.resolve(InfrastructureException.MESSAGE_KEY),
            RequestIdFilter.current()
        );

        return ResponseEntity.status(status).body(body);
    }

    /**
     * Answers everything else that escaped, so no failure reaches the caller as a container's
     * default error page.
     *
     * <p>Spring's own request failures already carry a status, and a malformed body or an unmapped
     * path is the caller's to fix rather than a fault here. Those are rethrown for the resolver that
     * knows their status. Rethrowing the exception it was given is what an
     * {@code @ExceptionHandler} may do to decline.
     *
     * @param failure the failure to report
     * @return the envelope, under a status that names no cause
     * @throws Throwable the failure itself, when it already states its own status
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(Throwable failure) throws Throwable {
        boolean carriesItsOwnStatus = failure instanceof org.springframework.web.ErrorResponse;

        if (carriesItsOwnStatus) {
            throw failure;
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        LOG.error("Unhandled failure, answering with {}", status, failure);

        ErrorResponse body = ErrorResponse.of(
            status,
            this.resolve(UNEXPECTED_KEY),
            RequestIdFilter.current()
        );

        return ResponseEntity.status(status).body(body);
    }

    /**
     * Resolves a key against the request's locale, which Spring reads from {@code Accept-Language}.
     * A key with no entry throws.
     */
    private String resolve(String key) {
        return this.messages.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
