package be.zeldown.herobrinecmd.lib;

import lombok.NonNull;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;

public enum SenderType {

	PLAYER(EntityPlayer.class),
	CONSOLE(MinecraftServer.class),
	COMMAND_BLOCK(CommandBlockLogic.class),
	RCON(RConConsoleSource.class),
	ALL(null),
	NONE(null);

	private final Class<? extends ICommandSender> sender;

	SenderType(final Class<? extends ICommandSender> sender) {
		this.sender = sender;
	}

	public boolean isAllowed(final @NonNull ICommandSender sender) {
		if (this.sender == null) {
			return true;
		}
		return this.sender.isAssignableFrom(sender.getClass());
	}

	public static @NonNull SenderType get(final @NonNull ICommandSender sender) {
		for (final SenderType type : SenderType.values()) {
			if (type.isAllowed(sender) && type != ALL) {
				return type;
			}
		}
		return ALL;
	}

}