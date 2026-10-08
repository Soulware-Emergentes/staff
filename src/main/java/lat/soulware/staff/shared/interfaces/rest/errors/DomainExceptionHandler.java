package lat.soulware.staff.shared.interfaces.rest.errors;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;
import lat.soulware.staff.shared.domain.model.exceptions.DomainException;
import lat.soulware.staff.shared.domain.model.exceptions.EntityNotFoundException;
import lat.soulware.staff.shared.domain.model.exceptions.ForbiddenException;
import lat.soulware.staff.shared.domain.model.exceptions.UnauthenticatedException;
import lat.soulware.staff.shared.domain.model.exceptions.ValidationException;
import lat.soulware.staff.shared.interfaces.rest.errors.responses.ErrorResponse;
import lat.soulware.staff.shared.interfaces.rest.errors.responses.ValidationErrorResponse;

/**
 * States what each kind of domain failure means over HTTP. The domain says which kind a failure is;
 * this class is the only place that turns that into a status code and a response body, and it holds
 * for every module.
 *
 * <p>It covers the failures raised while handling an HTTP request. Another transport intercepts its
 * own failures at its own point and states its own mapping.
 *
 * <p>Declared at highest precedence, ahead of any advice carrying a broader catch.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DomainExceptionHandler {

    private final MessageSource messages;

    /**
     * @param messages resolves message keys against the bundles and the request's locale
     */
    public DomainExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Answers a failure that names the individual values it rejected, resolving the summary and
     * each field's reason.
     *
     * @param failure the failure to report
     * @return the per-field envelope, under the same status any rule violation carries
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handle(ValidationException failure) {
        HttpStatus status = statusOf(failure);
        List<ValidationErrorResponse.FieldError> fields = failure.getViolations().stream()
            .map(violation -> new ValidationErrorResponse.FieldError(
                violation.fieldKey(),
                this.resolve(violation.messageKey())
            ))
            .toList();

        return ResponseEntity.status(status)
            .body(ValidationErrorResponse.of(status, this.resolve(failure), fields));
    }

    /**
     * Answers every other domain failure.
     *
     * @param failure the failure to report
     * @return the envelope, under the status its kind maps to
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handle(DomainException failure) {
        HttpStatus status = statusOf(failure);

        return ResponseEntity.status(status)
            .body(ErrorResponse.of(status, this.resolve(failure)));
    }

    /** The mapping itself, and the only statement of it. */
    private static HttpStatus statusOf(DomainException failure) {
        return switch (failure) {
            case UnauthenticatedException _ -> HttpStatus.UNAUTHORIZED;
            case ForbiddenException _ -> HttpStatus.FORBIDDEN;
            case EntityNotFoundException _ -> HttpStatus.NOT_FOUND;
            case BusinessRuleViolationException _ -> HttpStatus.UNPROCESSABLE_CONTENT;
        };
    }

    /** Resolves a failure's own key against its arguments. */
    private String resolve(DomainException failure) {
        return this.resolve(failure.getMessageKey(), failure.getMessageArgs());
    }

    /**
     * Resolves a key against the request's locale, which Spring reads from {@code Accept-Language}.
     * A key with no entry throws.
     */
    private String resolve(String key, Object... args) {
        return this.messages.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
