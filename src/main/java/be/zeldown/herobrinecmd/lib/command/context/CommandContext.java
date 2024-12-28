package be.zeldown.herobrinecmd.lib.command.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.exception.CommandConditionException;
import be.zeldown.herobrinecmd.lib.command.parser.CommandParser;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SideOnly(Side.SERVER)
public final class CommandContext {

	private final ICommandSender sender;
	private final SenderType type;

	private final String command;
	private final String[] args;

	public static @NonNull CommandContext create(final @NonNull ICommandSender sender, final @NonNull ICommand command, final @NonNull String @NonNull [] args) {
		return new CommandContext(sender, SenderType.get(sender), command.getCommandName(), args);
	}

	public static @NonNull CommandContext create(final @NonNull ICommandSender sender, final @NonNull String command, final @NonNull String @NonNull [] args) {
		return new CommandContext(sender, SenderType.get(sender), command, args);
	}

	public final @NonNull CompletableFuture<Void> async(final @NonNull Runnable runnable) {
		return CompletableFuture.runAsync(runnable);
	}

	public final @NonNull CompletableFuture<Void> async(final @NonNull Runnable runnable, final @NonNull Executor executor) {
		return CompletableFuture.runAsync(runnable, executor);
	}

	public final @NonNull CommandContext condition(final @NonNull Supplier<Boolean> supplier, final @NonNull String message) {
		if (!supplier.get()) {
			throw new CommandConditionException(this, message);
		}

		return this;
	}

	public final @NonNull CommandContext breakLine() {
		this.sender.addChatMessage(new ChatComponentText(""));
		return this;
	}

	public final @NonNull CommandContext send(final @NonNull IChatComponent message) {
		this.sender.addChatMessage(message);
		return this;
	}

	public final @NonNull CommandContext send(final @NonNull String message) {
		this.sender.addChatMessage(new ChatComponentText("§8[§6Command§8] §r" + message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RESET)));
		return this;
	}

	public final @NonNull CommandContext success(final @NonNull String success) {
		this.sender.addChatMessage(new ChatComponentText("§8[§6Command§8] §a" + success).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)));
		return this;
	}

	public final @NonNull CommandContext error(final @NonNull String error) {
		this.sender.addChatMessage(new ChatComponentText("§8[§6Command§8] §c" + error).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
		return this;
	}

	public final @NonNull CommandContext kick(final @NonNull String reason) {
		if (this.sender instanceof EntityPlayerMP) {
			((EntityPlayerMP) this.sender).playerNetServerHandler.kickPlayerFromServer(reason);
		}
		return this;
	}

	public final EntityPlayerMP getPlayer() {
		if (this.sender instanceof EntityPlayerMP) {
			return (EntityPlayerMP) this.sender;
		}
		return null;
	}

	public final MinecraftServer getConsole() {
		if (this.sender instanceof MinecraftServer) {
			return (MinecraftServer) this.sender;
		}
		return null;
	}

	public final CommandBlockLogic getCommandBlock() {
		if (this.sender instanceof CommandBlockLogic) {
			return (CommandBlockLogic) this.sender;
		}
		return null;
	}

	public final RConConsoleSource getRcon() {
		if (this.sender instanceof RConConsoleSource) {
			return (RConConsoleSource) this.sender;
		}
		return null;
	}

	public final boolean isPlayer() {
		return this.type == SenderType.PLAYER;
	}

	public final boolean isConsole() {
		return this.type == SenderType.CONSOLE;
	}

	public final boolean isCommandBlock() {
		return this.type == SenderType.COMMAND_BLOCK;
	}

	public final boolean isRcon() {
		return this.type == SenderType.RCON;
	}

	public final <T> T get(final int index, final @NonNull Class<T> type) {
		if (index < 0 || index >= this.args.length) {
			return null;
		}

		return CommandParser.parseArgument(this.args[index], type);
	}

	public final String get(final int index) {
		if (index < 0 || index >= this.args.length) {
			return null;
		}
		return this.args[index];
	}

	public final String getFull() {
		return this.command + " " + String.join(" ", this.args);
	}

	public final int size() {
		return this.args.length;
	}

	public final int length() {
		return this.args.length;
	}

}