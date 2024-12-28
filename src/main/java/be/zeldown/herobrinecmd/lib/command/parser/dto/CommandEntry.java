package be.zeldown.herobrinecmd.lib.command.parser.dto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

@Getter
@RequiredArgsConstructor
public final class CommandEntry {

	public static final int PAGE_SIZE = 5;
	public static final String SPACER = "------------------------------------";

	private final Object       instance;
	private final String       command;
	private final String[]     aliases;
	private final String       description;
	private final String       permission;
	private final SenderType[] sender;
	private final boolean      help;

	private final List<SubCommandEntry> subCommandList;

	public void help(final @NonNull CommandContext context, int page) {
		final long count = this.subCommandList.stream().filter(subCommand -> subCommand.canExecute(context)).filter(SubCommandEntry::isHelp).count();
		if (count == 0) {
			context.error("Impossible de trouver une commande correspondante.");
			return;
		}

		final int maxPage = (int) Math.ceil((float) count / (float) CommandEntry.PAGE_SIZE);
		if (page < 0) {
			page = 0;
		} else if (page >= maxPage) {
			page = maxPage - 1;
		}

		final String header = "§8[§c/" + this.command + "§8][§7" + (page + 1) + "/" + maxPage + "§8]";
		final String spacer = "§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), (CommandEntry.SPACER.length() - (header.length() - 10)) / 2);
		final int length = header.length() - 10 + (spacer.length() - 4) * 2 - 1;

		final ComponentBuilder headerComponent = new ComponentBuilder(spacer + header + spacer);
		if (this.description != null && !this.description.isEmpty()) {
			headerComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7" + StringUtils.capitalize(this.description.endsWith(".") ? this.description : this.description + ".")).create())).color(ChatColor.GRAY);
		}

		context.breakLine();
		context.send(headerComponent.create());
		context.breakLine();

		int index = 0;
		for (final SubCommandEntry subCommand : this.subCommandList.stream().sorted(Comparator.comparing(SubCommandEntry::getPriority)).sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).sorted((o1, o2) -> Integer.compare(o1.getParameters().length, o2.getParameters().length)).collect(Collectors.toList())) {
			if (!subCommand.canExecute(context) || !subCommand.isHelp()) {
				continue;
			}

			if (index < page * CommandEntry.PAGE_SIZE || index >= (page + 1) * CommandEntry.PAGE_SIZE) {
				index++;
				continue;
			}

			subCommand.help(context);
			index++;
		}

		if (page != maxPage - 1) {
			context.breakLine();
			final ComponentBuilder nextComponent = new ComponentBuilder(" §8[§c»§8] §7Afficher la page suivante");
			nextComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §cCliquez §7pour afficher la page suivante").create()));
			nextComponent.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command + " help " + (page + 2)));
			nextComponent.color(ChatColor.GRAY);
			context.send(nextComponent.create());
		}

		if (page != 0) {
			if (page == maxPage - 1) {
				context.breakLine();
			}
			final ComponentBuilder previousComponent = new ComponentBuilder(" §8[§c«§8] §7Afficher la page précédente");
			previousComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §cCliquez §7pour afficher la page précédente").create()));
			previousComponent.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command + " help " + page));
			previousComponent.color(ChatColor.GRAY);
			context.send(previousComponent.create());
		}

		context.breakLine();
		context.send(new ComponentBuilder("§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), length)).create());
		context.breakLine();
	}

	public SubCommandEntry getRoot() {
		return this.subCommandList.stream().filter(SubCommandEntry::isRoot).findFirst().orElse(null);
	}

}