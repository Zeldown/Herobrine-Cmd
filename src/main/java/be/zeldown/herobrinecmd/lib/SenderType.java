package be.zeldown.herobrinecmd.lib;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import lombok.NonNull;

public enum SenderType {

	PLAYER(Player.class),
	CONSOLE(ConsoleCommandSender.class),
	ALL(null),
	NONE(null);

	private final Class<? extends CommandSender> sender;

	SenderType(final Class<? extends CommandSender> sender) {
		this.sender = sender;
	}

	public boolean isAllowed(final @NonNull CommandSender sender) {
		if (this.sender == null) {
			return true;
		}
		return this.sender.isAssignableFrom(sender.getClass());
	}

	public static @NonNull SenderType get(final @NonNull CommandSender sender) {
		for (final SenderType type : SenderType.values()) {
			if (type.isAllowed(sender) && type != ALL) {
				return type;
			}
		}
		return ALL;
	}

}