package be.zeldown.herobrinecmd.lib.command.exception;

import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import lombok.NonNull;

public class CommandConditionException extends RuntimeException {

	private static final long serialVersionUID = -7994425798736533123L;

	public CommandConditionException(final @NonNull CommandContext context, final @NonNull String message) {
		super(message);
	}

}