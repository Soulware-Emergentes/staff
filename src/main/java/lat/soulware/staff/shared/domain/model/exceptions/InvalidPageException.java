package lat.soulware.staff.shared.domain.model.exceptions;

/** Thrown when a page is asked for before the first, or holding fewer than one or more than the cap. */
public final class InvalidPageException extends BusinessRuleViolationException {

    /**
     * @param page    the page asked for
     * @param size    the page size asked for
     * @param maxSize the largest page size allowed
     */
    public InvalidPageException(int page, int size, int maxSize) {
        super(Integer.toString(page), Integer.toString(size), Integer.toString(maxSize));
    }
}
