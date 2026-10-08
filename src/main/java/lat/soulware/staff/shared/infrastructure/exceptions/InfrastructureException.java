package lat.soulware.staff.shared.infrastructure.exceptions;

/**
 * Raised by an outbound adapter when the technology supplying a port fails in a way the model has
 * no opinion about: the database refused the connection, the gateway timed out, the broker rejected
 * the publish. Unchecked, so no port declares it and no layer between the adapter and the boundary
 * names it.
 *
 * <p>The detail and the cause are for the log and the stack trace. What reaches the caller is the
 * message the bundle defines for {@link #MESSAGE_KEY}, which names no technology and carries
 * nothing from either.
 *
 * <p>A failure the model does have an opinion about is a {@code DomainException} the module
 * declares, and the adapter throws that instead.
 */
public final class InfrastructureException extends RuntimeException {

    /** The single key this failure reports, resolved by whoever answers the caller. */
    public static final String MESSAGE_KEY = "error.shared.infrastructure";

    /**
     * @param detail what the adapter was attempting, for the log
     * @param cause  the technology's own failure
     */
    public InfrastructureException(String detail, Throwable cause) {
        super(detail, cause);
    }
}
