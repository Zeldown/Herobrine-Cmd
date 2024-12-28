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
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

@Getter
@RequiredArgsConstructor
public final class CommandEntry {

	public static final int PAGE_SIZE = 5;
	public static final String SPACER = "------------------------------------";

	private final Object       instance;
	private final String       command;
	private final String[]     aliases;
	private final String       description;
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

		final ChatComponentText headerComponent = new ChatComponentText(spacer + header + spacer);
		if (this.description != null && !this.description.isEmpty()) {
			final ChatStyle style = new ChatStyle();
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + StringUtils.capitalize(this.description.endsWith(".") ? this.description : this.description + "."))));
			style.setColor(EnumChatFormatting.GRAY);
			headerComponent.setChatStyle(style);
		}

		context.breakLine();
		context.send(headerComponent);
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
			final ChatComponentText nextComponent = new ChatComponentText(" §8[§c»§8] §7Afficher la page suivante");
			final ChatStyle style = new ChatStyle();
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour afficher la page suivante")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command + " help " + (page + 2)));
			style.setColor(EnumChatFormatting.GRAY);
			nextComponent.setChatStyle(style);
			context.send(nextComponent);
		}

		if (page != 0) {
			if (page == maxPage - 1) {
				context.breakLine();
			}
			final ChatComponentText previousComponent = new ChatComponentText(" §8[§c«§8] §7Afficher la page précédente");
			final ChatStyle style = new ChatStyle();
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour afficher la page précédente")));
			style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.command + " help " + page));
			style.setColor(EnumChatFormatting.GRAY);
			previousComponent.setChatStyle(style);
			context.send(previousComponent);
		}

		context.breakLine();
		context.send(new ChatComponentText("§7§m" + StringUtils.repeat(CommandEntry.SPACER.charAt(0), length)));
		context.breakLine();
	}

	public SubCommandEntry getRoot() {
		return this.subCommandList.stream().filter(SubCommandEntry::isRoot).findFirst().orElse(null);
	}

}