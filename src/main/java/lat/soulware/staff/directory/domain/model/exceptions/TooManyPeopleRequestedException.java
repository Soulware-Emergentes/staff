package lat.soulware.staff.directory.domain.model.exceptions;

import lat.soulware.staff.shared.domain.model.exceptions.BusinessRuleViolationException;

/** Thrown when one lookup names more people than the directory resolves at once. */
public final class TooManyPeopleRequestedException extends BusinessRuleViolationException {

    /**
     * @param limit     how many one lookup may name
     * @param requested how many it named
     */
    public TooManyPeopleRequestedException(int limit, int requested) {
        super(limit, requested);
    }
}
