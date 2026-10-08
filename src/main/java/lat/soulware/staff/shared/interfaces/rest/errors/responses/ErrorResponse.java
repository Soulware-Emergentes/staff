package lat.soulware.staff.shared.interfaces.rest.errors.responses;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Envelope returned for any domain failure. The status appears in the body as well as on the
 * response.
 *
 * @param status    the HTTP status code
 * @param error     the status reason phrase
 * @param message   the failure, resolved against the caller's locale
 * @param reference the request identifier the failure was logged under, omitted when absent
 * @param timestamp when the response was built
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    String reference,
    Instant timestamp
) {

    /**
     * Builds an envelope for a failure that leaves no log entry to point at.
     *
     * @param status  the status this failure maps to
     * @param message the resolved message
     * @return the envelope to serialize
     */
    public static ErrorResponse of(HttpStatus status, String message) {
        return of(status, message, null);
    }

    /**
     * Builds an envelope from the status the adapter chose, taking the code and the reason phrase
     * from it.
     *
     * @param status    the status this failure maps to
     * @param message   the resolved message
     * @param reference the request identifier the failure was logged under
     * @return the envelope to serialize
     */
    public static ErrorResponse of(HttpStatus status, String message, String reference) {
        return new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message,
            reference,
            Instant.now()
        );
    }
}
