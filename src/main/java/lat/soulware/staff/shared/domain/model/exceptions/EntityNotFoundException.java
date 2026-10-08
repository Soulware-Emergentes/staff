package lat.soulware.staff.shared.domain.model.exceptions;

/**
 * Thrown when a lookup by identity finds nothing.
 *
 * <p>Declare one subtype per aggregate, named for what was missing.
 */
public abstract non-sealed class EntityNotFoundException extends DomainException {

    /**
     * @param messageArgs values interpolated into the resolved message, in placeholder order
     */
    protected EntityNotFoundException(Object... messageArgs) {
        super(messageArgs);
    }
}
