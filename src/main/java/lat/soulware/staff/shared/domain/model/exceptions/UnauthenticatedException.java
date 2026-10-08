package lat.soulware.staff.shared.domain.model.exceptions;

/**
 * Thrown when a request carries no usable authenticated identity: no credential, an invalid one, or
 * one whose subject cannot be parsed.
 *
 * <p>The only kind that is concrete, so throwing it directly covers the common case. Subclass it
 * where a credential failure has a name and a message of its own.
 */
public non-sealed class UnauthenticatedException extends DomainException {

    /**
     * @param messageArgs values interpolated into the resolved message, in placeholder order
     */
    public UnauthenticatedException(Object... messageArgs) {
        super(messageArgs);
    }
}
