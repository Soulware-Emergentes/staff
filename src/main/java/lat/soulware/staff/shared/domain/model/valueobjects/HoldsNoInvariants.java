package lat.soulware.staff.shared.domain.model.valueobjects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a value object constrains nothing beyond what its components already constrain, so
 * it validates nothing on construction.
 *
 * <p>Carry it only on a record whose every component arrives validated, which is the one case where
 * a compact constructor would have no work to do. A record holding a raw type, a collection, or an
 * array has an invariant to enforce and writes the constructor instead.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HoldsNoInvariants {
}