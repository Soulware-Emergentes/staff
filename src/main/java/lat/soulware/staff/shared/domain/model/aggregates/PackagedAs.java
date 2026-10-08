package lat.soulware.staff.shared.domain.model.aggregates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The package segment an aggregate owns, declared once and compared against every package named for
 * it: {@code application.commands.<value>}, {@code interfaces.rest.<value>}, and
 * {@code infrastructure.persistence.<value>}.
 *
 * <p>An event names the same segment through {@code EmittedBy}, since an event has no package of
 * its own saying which aggregate announced it.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PackagedAs {

    /** One package segment, in the number and spelling the packages use, carrying no dots. */
    String value();
}