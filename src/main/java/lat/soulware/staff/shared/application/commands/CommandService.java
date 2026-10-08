package lat.soulware.staff.shared.application.commands;

import lat.soulware.staff.shared.application.commands.intent.CommandIntent;

/**
 * Marker interface for command services. A command service is the write entry point for one
 * aggregate: it injects that aggregate's handlers and forwards each command to the one that handles
 * it.
 *
 * <p>Implement as a class whose methods repeat the handler signatures they forward to: one
 * {@link CommandIntent} in, nothing or the identity of a created aggregate out. It holds no
 * {@code @Transactional} of its own.
 */
public interface CommandService {
}
