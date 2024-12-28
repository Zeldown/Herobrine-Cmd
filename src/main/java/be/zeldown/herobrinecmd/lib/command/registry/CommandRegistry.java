package be.zeldown.herobrinecmd.lib.command.registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import be.zeldown.herobrinecmd.lib.command.AnnotatedCommand;
import be.zeldown.herobrinecmd.lib.command.parser.CommandParser;
import be.zeldown.herobrinecmd.lib.command.parser.dto.CommandEntry;
import lombok.NonNull;

public final class CommandRegistry {

	private static final List<Class<?>> registering = new ArrayList<>();
	private static final Map<String, CommandEntry> commands = new HashMap<>();

	private static boolean started = false;

	public static void register() {
		CommandRegistry.started = true;
		for (final Class<?> clazz : CommandRegistry.registering) {
			CommandRegistry.registerCommand(clazz);
		}
		CommandRegistry.registering.clear();
	}

	public static void register(final Class<?>... classes) {
		for (final Class<?> clazz : classes) {
			CommandRegistry.registerCommand(clazz);
		}
	}

	public static void register(final Object... objects) {
		for (final Object object : objects) {
			CommandRegistry.registerCommand(object.getClass());
		}
	}

	private static void registerCommand(final Class<?> clazz) {
		final CommandEntry command = CommandParser.parseCommand(clazz);

		if (CommandRegistry.started) {
			try {
				final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				bukkitCommandMap.setAccessible(true);

				final CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
				commandMap.register(command.getCommand(), new AnnotatedCommand(command));

				CommandRegistry.commands.put(command.getCommand(), command);
				System.out.println("[CommandRegistry] Command " + command.getCommand() + " registered successfully.");
			} catch(final Exception e) {
				System.out.println("[CommandRegistry] Error while registering command " + command.getCommand() + ".");
				e.printStackTrace();
			}
			return;
		}

		CommandRegistry.registering.add(clazz);
	}

	public static CommandEntry getCommand(final @NonNull String command) {
		return CommandRegistry.commands.get(command.startsWith("/") ? command.substring(1) : command);
	}

}