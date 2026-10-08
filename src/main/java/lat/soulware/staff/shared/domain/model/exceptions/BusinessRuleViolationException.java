package lat.soulware.staff.shared.domain.model.exceptions;

/**
 * Thrown when an explicit business rule is violated inside a domain operation.
 *
 * <p>Declare one subtype per rule.
 */
public abstract non-sealed class BusinessRuleViolationException extends DomainException {

    /**
     * @param messageArgs values interpolated into the resolved message, in placeholder order
     */
    protected BusinessRuleViolationException(Object... messageArgs) {
        super(messageArgs);
    }
}
