package lat.soulware.staff.shared.application.commands.handlers;

/**
 * Marker interface for command handlers. A handler answers one command: it unpacks the values the
 * command carries, calls the aggregate with them, and owns the transaction the transition runs in.
 *
 * <p>Implement as a class carrying {@code @Transactional}, exposing one public method named
 * {@code handle} that takes the command the class is named for and returns nothing, or the identity
 * of an aggregate it created.
 */
public interface CommandHandler {
}
