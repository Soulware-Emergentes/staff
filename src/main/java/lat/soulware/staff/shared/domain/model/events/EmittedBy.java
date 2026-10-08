package lat.soulware.staff.shared.domain.model.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lat.soulware.staff.shared.domain.model.aggregates.PackagedAs;

/**
 * The aggregate that announces this event, named by the package segment its root declares with
 * {@link PackagedAs}.
 *
 * <p>Every event carries it, wherever the event lives. An event a module keeps to itself sits in
 * {@code domain.model.events}; one another module listens to sits in {@code publishedlanguage}.
 * Neither package says whose announcement it is, so the annotation is what attributes it, and what
 * an inbound handler's {@code interfaces.events.<emitter>} directory is compared against.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EmittedBy {

    /** One package segment, in the number and spelling the aggregate declared, carrying no dots. */
    String value();
}
