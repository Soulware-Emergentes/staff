package lat.soulware.staff.shared.application.commands.intent;

/**
 * Marker interface for commands. A command is a request for one state transition, named in the
 * imperative, and carries the values that transition needs.
 *
 * <p>Implement as a {@code record} whose components are value objects.
 *
 * <p>A command stops at the handler, which unpacks it and calls the aggregate with the values that
 * aggregate needs. Turning raw input into those values happens earlier, at the inbound adapter.
 */
public interface CommandIntent {
}
