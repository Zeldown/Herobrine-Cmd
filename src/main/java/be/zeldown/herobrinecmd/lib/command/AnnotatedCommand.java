package be.zeldown.herobrinecmd.lib.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.command.exception.CommandConditionException;
import be.zeldown.herobrinecmd.lib.command.parser.dto.CommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.SubCommandParameter;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.SubCommandScore;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

@Getter
public final class AnnotatedCommand extends Command {

	private final CommandEntry command;

	public AnnotatedCommand(final CommandEntry command) {
		super(command.getCommand(), command.getDescription(), command.getCommand(), Arrays.asList(command.getAliases()));
		this.command = command;
	}

	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		final CommandContext context = CommandContext.create(sender, this.command.getCommand(), args);
		if (!context.hasPermission(this.command.getPermission())) {
			context.error("Vous n'avez pas la permission d'executer cette commande.");
			return true;
		}

		if (args.length == 0) {
			final SubCommandEntry rootSubCommand = this.command.getRoot();
			if (rootSubCommand != null && rootSubCommand.canExecute(context)) {
				try {
					rootSubCommand.execute(this.command, context);
				} catch (final Exception e) {
					if (e instanceof InvocationTargetException && ((InvocationTargetException) e).getTargetException() instanceof CommandConditionException) {
						final CommandConditionException condition = (CommandConditionException) ((InvocationTargetException) e).getTargetException();
						final String error = condition.getMessage().substring(0, 1).toLowerCase() + condition.getMessage().substring(1);

						context.send(new ComponentBuilder("§8[§6Command§8] §cUne erreur est survenue, " + error)
								.color(ChatColor.RED)
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §cCliquez §7pour modifier la commande.").create()))
								.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + context.getFull()))
								.create());
						return true;
					}

					context.error("Une erreur est survenue lors de l'execution de la commande.");
					e.printStackTrace();
				}
				return true;
			}
		}

		SubCommandEntry bestMatch = null;
		SubCommandScore bestMatchScore = null;
		for (final SubCommandEntry subCommand : this.command.getSubCommandList()) {
			if (subCommand.isRoot() || !subCommand.canExecute(context)) {
				continue;
			}

			final SubCommandScore score = subCommand.score(context);
			if (score.equals(SubCommandScore.ZERO)) {
				continue;
			}

			if (bestMatch == null) {
				bestMatch = subCommand;
				bestMatchScore = score;
				continue;
			}

			final float bestMatchPercentage = bestMatchScore.getPercentage();
			final float currentPercentage = score.getPercentage();
			if (currentPercentage > bestMatchPercentage || currentPercentage == bestMatchPercentage && score.getScore() > bestMatchScore.getScore()) {
				bestMatch = subCommand;
				bestMatchScore = score;
				continue;
			}

			if (currentPercentage == bestMatchPercentage && score.getScore() == bestMatchScore.getScore() && score.getMaxScore() < bestMatchScore.getMaxScore()) {
				bestMatch = subCommand;
				bestMatchScore = score;
				continue;
			}

			if (currentPercentage == bestMatchPercentage && score.getScore() == bestMatchScore.getScore() && score.getMaxScore() == bestMatchScore.getMaxScore()) {
				if (subCommand.getParameters().length > bestMatch.getParameters().length) {
					bestMatch = subCommand;
					bestMatchScore = score;
					continue;
				}
			}
		}

		if (bestMatch != null && bestMatchScore.getPercentage() >= 0.5F) {
			if (bestMatch.test(context)) {
				try {
					bestMatch.execute(this.command, context);
				} catch (final Exception e) {
					if (e instanceof InvocationTargetException && ((InvocationTargetException) e).getTargetException() instanceof CommandConditionException) {
						final CommandConditionException condition = (CommandConditionException) ((InvocationTargetException) e).getTargetException();
						final String error = condition.getMessage().substring(0, 1).toLowerCase() + condition.getMessage().substring(1);

						context.send(new ComponentBuilder("§8[§6Command§8] §cUne erreur est survenue, " + error)
								.color(ChatColor.RED)
								.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §cCliquez §7pour modifier la commande.").create()))
								.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + context.getFull()))
								.create());
						return true;
					}

					context.error("Une erreur est survenue lors de l'execution de la commande.");
					e.printStackTrace();
				}
				return true;
			}

			final String header = "§8[§c/" + this.command.getCommand() + "§8]";
			final String spacer = "§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), (CommandEntry.SPACER.length() - (header.length() - 6)) / 2);
			final int length = header.length() - 6 + (spacer.length() - 4) * 2 - 1;

			final ComponentBuilder headerComponent = new ComponentBuilder(spacer + header + spacer);
			if (this.command.getDescription() != null && !this.command.getDescription().isEmpty()) {
				headerComponent
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7" + StringUtils.capitalize(this.command.getDescription().endsWith(".") ? this.command.getDescription() : this.command.getDescription() + ".")).create()))
				.color(ChatColor.GRAY);
			}

			context.breakLine();
			context.send(headerComponent.create());
			context.breakLine();

			context.send(new ComponentBuilder(" §8» §cLa syntaxe de la commande est incorrecte.").color(ChatColor.RED).create());
			bestMatch.help(context);

			context.breakLine();
			context.send(new ComponentBuilder(" §8[§c?§8] §7Afficher la page d'aide")
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §cCliquez §7pour afficher la page d'aide.").create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command.getCommand() + " help"))
					.color(ChatColor.GRAY)
					.create());

			context.breakLine();
			context.send(new ComponentBuilder("§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), length)).create());
			context.breakLine();
			return true;
		}

		if (!this.command.isHelp()) {
			context.error("Impossible de trouver une commande correspondante.");
			return true;
		}

		this.command.help(context, 0);
		return true;
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws IllegalArgumentException {
		final CommandContext context = CommandContext.create(sender, this.command.getCommand(), args);
		final List<String> list = new ArrayList<>();

		if (args.length == 0 || args.length == 1 && args[0].isEmpty()) {
			for (final SubCommandEntry subCommand : this.command.getSubCommandList()) {
				if (!subCommand.canExecute(context) || subCommand.getParameters().length == 0) {
					continue;
				}

				list.addAll(subCommand.getParameters()[0].getTabComplete(context));
			}
		} else {
			for (final SubCommandEntry subCommand : this.command.getSubCommandList()) {
				if (!subCommand.canExecute(context) || args.length > subCommand.getParameters().length) {
					continue;
				}

				boolean valid = true;
				for (int i = 0; i < args.length - 1; i++) {
					final String arg = args[i];
					final SubCommandParameter parameter = subCommand.getParameters()[i];
					if (parameter == null) {
						continue;
					}

					if (!parameter.test(arg)) {
						valid = false;
						break;
					}
				}

				if (!valid) {
					continue;
				}

				final SubCommandParameter parameter = subCommand.getParameters()[args.length - 1];
				if (parameter == null) {
					continue;
				}

				list.addAll(parameter.getTabComplete(context));
			}
		}

		if (list.isEmpty() || args.length == 0) {
			return list;
		}

		final String last = args[args.length - 1];
		if (last.isEmpty()) {
			return list;
		}

		return list.stream().filter(s -> s.regionMatches(true, 0, last, 0, last.length())).collect(Collectors.toList());
	}

}