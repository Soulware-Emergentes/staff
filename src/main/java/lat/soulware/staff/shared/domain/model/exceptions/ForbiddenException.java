package lat.soulware.staff.shared.domain.model.exceptions;

/**
 * Thrown when an authenticated requester lacks the rights for an action, holding insufficient
 * permission level or reaching for a resource that is not theirs.
 *
 * <p>Declare one subtype per denial.
 */
public abstract non-sealed class ForbiddenException extends DomainException {

    /**
     * @param messageArgs values interpolated into the resolved message, in placeholder order
     */
    protected ForbiddenException(Object... messageArgs) {
        super(messageArgs);
    }
}
