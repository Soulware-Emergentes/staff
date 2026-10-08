package lat.soulware.staff.shared.interfaces.rest.errors.responses;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

/**
 * Envelope for a failure that names the individual values it rejected. Carries the same fields as
 * {@link ErrorResponse} plus one entry per offending field.
 *
 * @param status     the HTTP status code
 * @param error      the status reason phrase
 * @param message    the failure as a whole, resolved against the caller's locale
 * @param violations one entry per rejected field
 * @param timestamp  when the response was built
 */
public record ValidationErrorResponse(
    int status,
    String error,
    String message,
    List<FieldError> violations,
    Instant timestamp
) {

    /**
     * One rejected field, ready to render. The same pairing the domain reported, with the reason
     * already resolved.
     *
     * @param field   identifies the field to the client, matching the name it submitted
     * @param message why this value was rejected, resolved against the caller's locale
     */
    public record FieldError(String field, String message) {}

    /**
     * Builds an envelope from the status the adapter chose, taking the code and the reason phrase
     * from it.
     *
     * @param status     the status this failure maps to
     * @param message    the resolved summary message
     * @param violations the rejected fields, copied defensively
     * @return the envelope to serialize
     */
    public static ValidationErrorResponse of(
        HttpStatus status,
        String message,
        List<FieldError> violations
    ) {
        return new ValidationErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message,
            List.copyOf(violations),
            Instant.now()
        );
    }
}
