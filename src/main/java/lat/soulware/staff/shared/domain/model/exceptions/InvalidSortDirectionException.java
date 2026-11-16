package lat.soulware.staff.shared.domain.model.exceptions;

/** Thrown when a sort direction is neither {@code asc} nor {@code desc}. */
public final class InvalidSortDirectionException extends BusinessRuleViolationException {

    /**
     * @param direction the direction asked for
     */
    public InvalidSortDirectionException(String direction) {
        super(direction);
    }
}
