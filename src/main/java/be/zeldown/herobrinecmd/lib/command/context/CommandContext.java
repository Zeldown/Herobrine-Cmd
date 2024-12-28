package be.zeldown.herobrinecmd.lib.command.context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.exception.CommandConditionException;
import be.zeldown.herobrinecmd.lib.command.parser.CommandParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandContext {

	private final CommandSender sender;
	private final SenderType type;

	private final String command;
	private final String[] args;

	public static @NonNull CommandContext create(final @NonNull CommandSender sender, final @NonNull String command, final @NonNull String @NonNull [] args) {
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
		this.sender.sendMessage("");
		return this;
	}

	public final @NonNull CommandContext send(final @NonNull BaseComponent... message) {
		if (this.sender instanceof Player) {
			((Player) this.sender).spigot().sendMessage(message);
		} else {
			this.sender.sendMessage(BaseComponent.toPlainText(message));
		}
		return this;
	}

	public final @NonNull CommandContext send(final @NonNull String message) {
		this.sender.sendMessage("§8[§6Command§8] §r" + message);
		return this;
	}

	public final @NonNull CommandContext success(final @NonNull String success) {
		this.sender.sendMessage("§8[§6Command§8] §a" + success);
		return this;
	}

	public final @NonNull CommandContext error(final @NonNull String error) {
		this.sender.sendMessage("§8[§6Command§8] §c" + error);
		return this;
	}

	public final @NonNull CommandContext kick(final @NonNull String reason) {
		if (this.sender instanceof Player) {
			((Player) this.sender).kickPlayer(reason);
		}
		return this;
	}

	public final boolean hasPermission(final String permission) {
		if (permission == null || permission.isEmpty()) {
			return true;
		}

		if (!this.sender.hasPermission(permission)) {
			return false;
		}

		return true;
	}

	public final Player getPlayer() {
		if (this.sender instanceof Player) {
			return (Player) this.sender;
		}
		return null;
	}

	public final ConsoleCommandSender getConsole() {
		if (this.sender instanceof ConsoleCommandSender) {
			return (ConsoleCommandSender) this.sender;
		}
		return null;
	}

	public final boolean isPlayer() {
		return this.type == SenderType.PLAYER;
	}

	public final boolean isConsole() {
		return this.type == SenderType.CONSOLE;
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