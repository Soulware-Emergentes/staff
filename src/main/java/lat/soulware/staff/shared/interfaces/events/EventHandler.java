package lat.soulware.staff.shared.interfaces.events;

/**
 * Marker interface for inbound event handlers. A handler answers one event from another module: it
 * turns the fact that event reports into one command against its own aggregate and hands that
 * command to the command service.
 *
 * <p>Implement as a class exposing one public method named {@code on}, annotated
 * {@code @ApplicationModuleListener}, taking the event the class is named for and returning
 * nothing. The annotation supplies the transaction and the asynchrony. A handler declares neither
 * itself.
 */
public interface EventHandler {
}
