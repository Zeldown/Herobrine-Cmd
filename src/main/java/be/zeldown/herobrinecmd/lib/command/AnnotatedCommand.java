package be.zeldown.herobrinecmd.lib.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.command.exception.CommandConditionException;
import be.zeldown.herobrinecmd.lib.command.parser.dto.CommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.SubCommandParameter;
import be.zeldown.herobrinecmd.lib.command.parser.dto.SubCommandEntry.SubCommandScore;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import scala.actors.threadpool.Arrays;

@Getter
@RequiredArgsConstructor
public final class AnnotatedCommand implements ICommand {

	private final CommandEntry command;

	@Override
	public String getCommandName() {
		return this.command.getCommand();
	}

	@Override
	public void processCommand(final @NonNull ICommandSender sender, final @NonNull String @NonNull [] args) {
		final CommandContext context = CommandContext.create(sender, this.getCommandName(), args);
		if (args.length == 0) {
			final SubCommandEntry rootSubCommand = this.command.getRoot();
			if (rootSubCommand != null && rootSubCommand.canExecute(context)) {
				try {
					rootSubCommand.execute(this.command, context);
				} catch (final Exception e) {
					if (e instanceof InvocationTargetException && ((InvocationTargetException) e).getTargetException() instanceof CommandConditionException) {
						final CommandConditionException condition = (CommandConditionException) ((InvocationTargetException) e).getTargetException();
						final String error = condition.getMessage().substring(0, 1).toLowerCase() + condition.getMessage().substring(1);

						final ChatComponentText component = new ChatComponentText("§8[§6Command§8] §cUne erreur est survenue, " + error);
						final ChatStyle style = new ChatStyle();
						style.setColor(EnumChatFormatting.RED);
						style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour modifier la commande.")));
						style.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + context.getFull()));
						component.setChatStyle(style);
						context.send(component);
						return;
					}

					context.error("Une erreur est survenue lors de l'execution de la commande.");
					e.printStackTrace();
				}
				return;
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

						final ChatComponentText component = new ChatComponentText("§8[§6Command§8] §cUne erreur est survenue, " + error);
						final ChatStyle style = new ChatStyle();
						style.setColor(EnumChatFormatting.RED);
						style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour modifier la commande.")));
						style.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + context.getFull()));
						component.setChatStyle(style);
						context.send(component);
						return;
					}

					context.error("Une erreur est survenue lors de l'execution de la commande.");
					e.printStackTrace();
				}
				return;
			}

			final String header = "§8[§c/" + this.command.getCommand() + "§8]";
			final String spacer = "§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), (CommandEntry.SPACER.length() - (header.length() - 6)) / 2);
			final int length = header.length() - 6 + (spacer.length() - 4) * 2 - 1;

			final ChatComponentText headerComponent = new ChatComponentText(spacer + header + spacer);
			if (this.command.getDescription() != null && !this.command.getDescription().isEmpty()) {
				final ChatStyle style = new ChatStyle();
				style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + StringUtils.capitalize(this.command.getDescription().endsWith(".") ? this.command.getDescription() : this.command.getDescription() + "."))));
				style.setColor(EnumChatFormatting.GRAY);
				headerComponent.setChatStyle(style);
			}

			context.breakLine();
			context.send(headerComponent);
			context.breakLine();

			context.send(new ChatComponentText(" §8» §cLa syntaxe de la commande est incorrecte.").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			bestMatch.help(context);

			context.breakLine();
			final ChatComponentText nextComponent = new ChatComponentText(" §8[§c?§8] §7Afficher la page d'aide");
			final ChatStyle style = new ChatStyle();
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour afficher la page d'aide")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command.getCommand() + " help"));
			style.setColor(EnumChatFormatting.GRAY);
			nextComponent.setChatStyle(style);
			context.send(nextComponent);

			context.breakLine();
			context.send(new ChatComponentText("§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), length)));
			context.breakLine();
			return;
		}

		if (!this.command.isHelp()) {
			context.error("Impossible de trouver une commande correspondante.");
			return;
		}

		this.command.help(context, 0);
	}

	@Override
	public String getCommandUsage(final ICommandSender sender) {
		return "/" + this.getCommandName();
	}

	@Override
	public List<?> getCommandAliases() {
		return Arrays.asList(this.command.getAliases());
	}

	@Override
	public List<?> addTabCompletionOptions(final ICommandSender sender, final String[] args) {
		final CommandContext context = CommandContext.create(sender, this.getCommandName(), args);
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

	@Override
	public boolean canCommandSenderUseCommand(final ICommandSender sender) {
		return true;
	}

	@Override
	public boolean isUsernameIndex(final String[] args, final int index) {
		return false;
	}

	@Override
	public int compareTo(final Object o) {
		return this.getCommandName().compareTo(((ICommand) o).getCommandName());
	}

}