package be.zeldown.herobrinecmd.lib.command.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.annotation.Command;
import be.zeldown.herobrinecmd.lib.command.annotation.CommandParameter;
import be.zeldown.herobrinecmd.lib.command.annotation.SubCommand;
import be.zeldown.herobrinecmd.lib.command.annotation.SubCommandClass;
import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.command.parser.dto.CommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.DynamicSubCommandParameter;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.StaticSubCommandParameter;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.SubCommandParameter;
import be.zeldown.herobrinecmd.lib.entity.OfflinePlayer;
import be.zeldown.herobrinecmd.lib.utils.FastUUID;
import lombok.NonNull;

public final class CommandParser {

	public static CommandEntry parseCommand(final @NonNull Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Command.class)) {
			throw new IllegalArgumentException("Unable to parse command: " + clazz.getName()  + " as it does not have the @Command annotation");
		}

		final Command command = clazz.getAnnotation(Command.class);

		final String[] aliases = command.command();
		if (aliases.length == 0) {
			throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as it does not have any aliases");
		}

		if (aliases.length > 0) {
			for (int i = 0; i < aliases.length; i++) {
				aliases[i] = aliases[i].startsWith("/") ? aliases[i].substring(1) : aliases[i];
				if (aliases[i].isEmpty()) {
					throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as an alias is empty");
				}
			}
		}

		final String commandName = aliases[0];
		final String description = command.description();
		final String permission = command.permission();
		final SenderType[] sender = command.sender();
		final boolean help = command.help();

		if (commandName.isEmpty()) {
			throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as the command name is empty");
		}

		if (sender == null) {
			throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as the sender type is null");
		}

		try {
			final Object instance = clazz.newInstance();
			final List<SubCommandEntry> subCommandList = CommandParser.parseSubCommands(clazz, sender, instance);

			if (subCommandList.isEmpty()) {
				throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as it does not have any sub-commands");
			}

			if (help) {
				subCommandList.add(new SubCommandEntry(commandName, "show help", permission, new SenderType[] {SenderType.ALL}, true, Integer.MAX_VALUE, instance, null, new SubCommandParameter[] {new StaticSubCommandParameter("help"), new DynamicSubCommandParameter("[<page>]", "numéro de la page d'aide", "", new String[] {}, true, false, null, Integer.class)}).callback((ctx, cmd) -> cmd.help(ctx, ctx.get(1, Integer.class) == null ? 0 : ctx.get(1, Integer.class).intValue() - 1)));
			}
			return new CommandEntry(instance, commandName, aliases, description, permission, sender, help, subCommandList.stream().sorted(Comparator.comparing(SubCommandEntry::getPriority).reversed()).collect(Collectors.toList()));
		} catch (final Exception e) {
			throw new IllegalArgumentException("Unable to parse command: " + clazz.getName() + " as it does not have a default constructor");
		}
	}

	public static List<SubCommandEntry> parseSubCommands(final Class<?> clazz, final @NonNull SenderType @NonNull[] defaultSenderTypes, final Object instance) {
		final List<SubCommandEntry> subCommandList = new ArrayList<>();
		for (final Method method : clazz.getMethods()) {
			if (!method.isAnnotationPresent(SubCommand.class)) {
				continue;
			}

			final SubCommandEntry subCommand = CommandParser.parseSubCommand(defaultSenderTypes, instance, method);
			if (subCommand == null) {
				continue;
			}

			subCommandList.add(subCommand);
		}

		for (final Field field : clazz.getDeclaredFields()) {
			if (!field.isAnnotationPresent(SubCommandClass.class)) {
				continue;
			}

			try {
				field.setAccessible(true);
				Object fieldInstance = field.get(instance);
				if (fieldInstance == null) {
					fieldInstance = field.getType().newInstance();
					field.set(instance, fieldInstance);
				}

				subCommandList.addAll(CommandParser.parseSubCommands(field.getType(), defaultSenderTypes, fieldInstance));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		return subCommandList;
	}

	public static SubCommandEntry parseSubCommand(final @NonNull SenderType @NonNull[] defaultSenderTypes, final Object instance, final @NonNull Method method) {
		if (!method.isAnnotationPresent(SubCommand.class)) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as it does not have the @SubCommand annotation");
		}

		method.setAccessible(true);

		final SubCommand command = method.getAnnotation(SubCommand.class);
		final List<String> arguments = Arrays.asList(command.command().split(" "));

		final String commandName = arguments.get(0).startsWith("/") ? arguments.get(0).substring(1) : arguments.get(0);
		final String description = command.description();
		final String permission = command.permission();
		final SenderType[] sender = command.sender().length == 0 || command.sender()[0] == SenderType.NONE ? defaultSenderTypes : command.sender();
		final boolean help = command.help();
		final int priority = command.priority();

		if (commandName.isEmpty()) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the command name is empty");
		}

		if ("help".equalsIgnoreCase(commandName)) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the command name is reserved");
		}

		if (sender == null) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the sender type is null");
		}

		if (method.getParameterCount() == 0 || !method.getParameterTypes()[0].isAssignableFrom(CommandContext.class)) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the first parameter is not CommandContext");
		}

		int count = 0;
		final List<SubCommandParameter> parameters = new ArrayList<>();
		for (int i = 1; i < arguments.size(); i++) {
			final String argument = arguments.get(i);
			if (argument.startsWith("<") && argument.endsWith(">") || argument.startsWith("[<") && argument.endsWith(">]")) {
				final Parameter parameter = method.getParameters()[count + 1];
				Class<?> type = method.getParameterTypes()[count + 1];

				if (type == null || type == CommandContext.class) {
					throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the parameter at index " + i + " is not a valid type");
				}

				final boolean optional = argument.startsWith("[<") && argument.endsWith(">]");
				if (optional && !type.equals(Optional.class) || type.equals(Optional.class) && !optional) {
					throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as an optional argument is not Optional<T>");
				}

				if (optional && i != arguments.size() - 1) {
					throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as an optional argument is not the last argument");
				}

				if (optional) {
					type = (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
				}

				final boolean infinite = type.equals(String[].class);
				if (infinite && i != arguments.size() - 1) {
					throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as an infinite argument is not the last argument");
				}

				final CommandParameter commandParameter = parameter.getAnnotation(CommandParameter.class);
				final String parameterDescription = commandParameter == null ? null : commandParameter.description();
				final String parameterError = commandParameter == null ? null : commandParameter.error();
				final String[] parameterAutocomplete = commandParameter == null ? null : commandParameter.autocomplete();
				parameters.add(new DynamicSubCommandParameter(argument, parameterDescription, parameterError, parameterAutocomplete, optional, infinite, parameter, type));
				count++;
				continue;
			}

			parameters.add(new StaticSubCommandParameter(argument));
		}

		if (count != method.getParameterCount() - 1) {
			throw new IllegalArgumentException("Unable to parse sub-command: " + method.getName() + " as the number of parameters does not match the number of arguments");
		}

		return new SubCommandEntry(commandName, description, permission, sender, help, priority, instance, method, parameters.toArray(new SubCommandParameter[0]));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parseArgument(final String argument, final @NonNull Class<T> type) {
		if (argument == null || argument.isEmpty()) {
			throw new IllegalArgumentException("Unable to parse argument: " + argument + " as it is null or empty");
		}

		try {
			if (type.isAssignableFrom(Player.class)) {
				if (FastUUID.isUUID(argument)) {
					try {
						final UUID uuid = FastUUID.from(argument);
						final Player player = Bukkit.getPlayer(uuid);
						if (player != null) {
							return type.cast(player);
						}
					} catch (final Exception e) {}
				}

				return type.cast(Bukkit.getPlayer(argument));
			}

			if (type.isAssignableFrom(OfflinePlayer.class)) {
				UUID uuid = null;
				if (FastUUID.isUUID(argument)) {
					try {
						uuid = FastUUID.from(argument);
					} catch (final Exception e) {}
				}

				OfflinePlayer player = null;
				if (uuid == null) {
					player = OfflinePlayer.load(argument).get();
				} else {
					player = OfflinePlayer.load(uuid).get();
				}

				if (player == null) {
					throw new IllegalArgumentException("Unable to parse argument: " + argument + " as it is not a valid player");
				}

				return type.cast(player);
			}

			if (type.isAssignableFrom(UUID.class)) {
				try {
					return type.cast(FastUUID.from(argument));
				} catch (final Exception e) {
					throw new IllegalArgumentException("Unable to parse argument: " + argument + " as it is not a valid UUID");
				}
			}

			if (type.isAssignableFrom(World.class)) {
				return type.cast(Bukkit.getWorld(argument));
			}

			if (type.isEnum()) {
				try {
					return (T) Enum.valueOf((Class<Enum>) type, argument.toUpperCase());
				} catch (final Exception | NoClassDefFoundError e) {
					try {
						final int ordinal = Integer.parseInt(argument);
						return type.getEnumConstants()[ordinal];
					} catch (final Exception | NoClassDefFoundError e1) {
						throw new IllegalArgumentException("Unable to parse argument: " + argument + " as it is not a valid enum");
					}
				}
			}

			if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
				return (T) Integer.valueOf(argument);
			}

			if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
				return (T) Double.valueOf(argument);
			}

			if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
				return (T) Float.valueOf(argument);
			}

			if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
				return (T) Long.valueOf(argument);
			}

			if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
				return (T) Short.valueOf(argument);
			}

			if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
				return (T) Byte.valueOf(argument);
			}

			if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
				return (T) Boolean.valueOf(argument);
			}

			return type.cast(argument);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Unable to parse argument: " + argument + " as it is not a valid " + type.getSimpleName());
		}
	}

}