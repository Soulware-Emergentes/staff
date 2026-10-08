package lat.soulware.staff.shared.domain.model.exceptions;

import java.util.List;

/**
 * Thrown when one or more values break their constraints, the input having been structurally
 * accepted. Exposes the per-field {@link FieldViolation} list.
 *
 * <p>Declare one subtype per validating operation.
 */
public abstract class ValidationException extends BusinessRuleViolationException {

    /**
     * One rejected field, stated as keys. The domain names which field failed and which message
     * describes the failure; resolving that message against a bundle and a locale belongs to
     * whoever reports the failure onwards.
     *
     * @param fieldKey   identifies the field to the client, matching the name it submitted
     * @param messageKey i18n key describing why this particular value was rejected
     */
    public record FieldViolation(String fieldKey, String messageKey) {}

    /** The rejected fields. */
    private final transient List<FieldViolation> violations;

    /**
     * @param violations the fields that were rejected, copied defensively
     */
    protected ValidationException(List<FieldViolation> violations) {
        this.violations = List.copyOf(violations);
    }

    /**
     * The rejected fields, in the order the validating operation collected them.
     *
     * @return the violations, unmodifiable
     */
    public List<FieldViolation> getViolations() {
        return this.violations;
    }
}
