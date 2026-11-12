package lat.soulware.staff.shared.domain.model.exceptions;

/** Thrown when a list is asked to be ordered by a column it does not sort by. */
public final class UnsortableColumnException extends BusinessRuleViolationException {

    /**
     * @param column   the column asked for
     * @param sortable the columns the list sorts by, comma separated
     */
    public UnsortableColumnException(String column, String sortable) {
        super(column, sortable);
    }
}
