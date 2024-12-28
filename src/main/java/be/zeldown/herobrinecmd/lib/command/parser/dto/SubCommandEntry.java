package be.zeldown.herobrinecmd.lib.command.parser.dto;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.command.parser.CommandParser;
import be.zeldown.herobrinecmd.lib.entity.OfflinePlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

@Getter
@RequiredArgsConstructor
public final class SubCommandEntry {

	private final String       name;
	private final String       description;
	private final SenderType[] sender;
	private final boolean      help;
	private final int          priority;

	private final Object instance;
	private final Method method;
	private final SubCommandParameter[] parameters;

	private BiConsumer<CommandContext, CommandEntry> callback;

	public void execute(final @NonNull CommandEntry command, final @NonNull CommandContext context) throws Exception {
		if (this.parameters.length == 0) {
			if (this.method != null) {
				this.method.invoke(this.instance, context);
			}

			if (this.callback != null) {
				this.callback.accept(context, command);
			}

			return;
		}

		if (this.method != null) {
			final List<Object> arguments = new ArrayList<>();
			arguments.add(context);
			for (int i = 0; i < this.parameters.length; i++) {
				final SubCommandParameter parameter = this.parameters[i];
				if (parameter instanceof StaticSubCommandParameter) {
					continue;
				}

				final DynamicSubCommandParameter dynamicParameter = (DynamicSubCommandParameter) parameter;
				if (dynamicParameter.isInfinite()) {
					final List<String> values = new ArrayList<>();
					for (int j = i; j < context.length(); j++) {
						values.add(context.get(j));
					}

					final String[] array = values.toArray(new String[0]);
					arguments.add(dynamicParameter.isOptional() ? i < context.size() ? Optional.of(array) : Optional.empty() : array);
					continue;
				}

				final Object value = context.get(i, dynamicParameter.getType());
				arguments.add(dynamicParameter.isOptional() ? value == null ? Optional.empty() : Optional.of(value) : value);
			}

			this.method.invoke(this.instance, arguments.toArray());
		}

		if (this.callback != null) {
			this.callback.accept(context, command);
		}
	}

	public void help(final @NonNull CommandContext context) {
		final ChatComponentText fullName = new ChatComponentText("§6/" + this.name);
		fullName.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));
		for (final SubCommandParameter parameter : this.parameters) {
			final ChatComponentText component = new ChatComponentText("§6" + parameter.getName());
			component.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));
			if (parameter instanceof DynamicSubCommandParameter) {
				final DynamicSubCommandParameter dynamicParameter = (DynamicSubCommandParameter) parameter;
				if (dynamicParameter.getDescription() != null && !dynamicParameter.getDescription().isEmpty()) {
					final ChatStyle style = component.getChatStyle();
					style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + StringUtils.capitalize(dynamicParameter.description.endsWith(".") ? dynamicParameter.description : dynamicParameter.description + "."))));
					component.setChatStyle(style);
				}
			}

			fullName.appendText(" ");
			fullName.appendSibling(component);
		}

		final ChatComponentText root = new ChatComponentText(" §8» ");
		root.appendSibling(fullName);
		if (this.description != null && !this.description.isEmpty()) {
			root.appendSibling(new ChatComponentText(" §7" + StringUtils.capitalize(this.description.endsWith(".") ? this.description : this.description + ".")).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		}

		final ChatStyle style = new ChatStyle();
		style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§8» §cCliquez §7pour compléter la commande.")));
		style.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullName.getUnformattedText().replace("§6", "").replace("§7", "")));
		root.setChatStyle(style);

		context.send(root);
	}

	public boolean canExecute(final @NonNull CommandContext context) {
		for (final SenderType sender : this.sender) {
			if (sender.isAllowed(context.getSender())) {
				return true;
			}
		}

		return false;
	}

	public boolean test(final @NonNull CommandContext context) {
		for (int i = 0; i < this.parameters.length; i++) {
			final SubCommandParameter parameter = this.parameters[i];
			if (parameter instanceof StaticSubCommandParameter) {
				if (i >= context.length() || !parameter.test(context.get(i))) {
					return false;
				}

				continue;
			}

			final DynamicSubCommandParameter dynamicParameter = (DynamicSubCommandParameter) parameter;
			if (!dynamicParameter.isOptional() && i >= context.length()) {
				return false;
			}

			if (dynamicParameter.isInfinite()) {
				for (int j = i; j < context.length(); j++) {
					if (!dynamicParameter.test(context.get(j))) {
						return false;
					}
				}

				return true;
			}

			if (!dynamicParameter.test(context.get(i))) {
				return false;
			}
		}

		return true;
	}

	public SubCommandScore score(final @NonNull CommandContext context) {
		if (context.length() > this.parameters.length) {
			return SubCommandScore.ZERO;
		}

		int score = 0;
		int maxScore = 0;
		for (int i = 0; i < this.parameters.length; i++) {
			final SubCommandParameter parameter = this.parameters[i];
			if (parameter instanceof StaticSubCommandParameter) {
				if (i >= context.length()) {
					score += 0;
					maxScore += 3;
					continue;
				}

				if (!parameter.test(context.get(i))) {
					return SubCommandScore.ZERO;
				}

				score += 3;
				maxScore += 3;
				continue;
			}

			final DynamicSubCommandParameter dynamicParameter = (DynamicSubCommandParameter) parameter;
			if (!dynamicParameter.isOptional() && i >= context.length()) {
				score += 0;
				maxScore += 2;
				continue;
			}

			if (dynamicParameter.isInfinite()) {
				for (int j = i; j < context.length(); j++) {
					if (!dynamicParameter.test(context.get(j))) {
						score += 1;
						maxScore += 2;
						continue;
					}
				}

				score += 2;
				maxScore += 2;
				continue;
			}

			if (!dynamicParameter.test(context.get(i))) {
				score += 1;
				maxScore += 2;
				continue;
			}

			score += 2;
			maxScore += 2;
		}

		return new SubCommandScore(score, maxScore);
	}

	public @NonNull SubCommandEntry callback(final BiConsumer<CommandContext, CommandEntry> callback) {
		this.callback = callback;
		return this;
	}

	public boolean isRoot() {
		return this.parameters.length == 0 || this.parameters[0] instanceof DynamicSubCommandParameter && ((DynamicSubCommandParameter) this.parameters[0]).isOptional();
	}

	@Getter
	@RequiredArgsConstructor
	public static final class DynamicSubCommandParameter implements SubCommandParameter {

		private final String name;
		private final String description;
		private final String error;
		private final String[] autocomplete;

		private final boolean optional;
		private final boolean infinite;

		private final Parameter parameter;
		private final Class<?> type;

		@Override
		public boolean test(final String argument) {
			if (this.optional && (argument == null || argument.isEmpty())) {
				return true;
			}

			final Class<?> type = this.infinite ? String.class : this.type;
			try {
				CommandParser.parseArgument(argument, type);
			} catch (final Exception e) {
				return false;
			}

			return true;
		}

		@Override
		public List<String> getTabComplete(final @NonNull CommandContext context) {
			if (this.autocomplete != null && this.autocomplete.length > 0 && !this.autocomplete[0].isEmpty()) {
				return Arrays.asList(this.autocomplete);
			}

			final List<String> list = new ArrayList<>();
			if (Number.class.isAssignableFrom(this.type) || this.type == int.class || this.type == double.class || this.type == float.class || this.type == long.class || this.type == short.class || this.type == byte.class) {
				list.add("0");
			} else if (Boolean.class.isAssignableFrom(this.type) || this.type == boolean.class) {
				list.add("true");
				list.add("false");
			} else if (Enum.class.isAssignableFrom(this.type)) {
				final Enum<?>[] constants = (Enum<?>[]) this.type.getEnumConstants();
				for (final Enum<?> constant : constants) {
					list.add(constant.name());
				}
			} else if ((EntityPlayer.class.isAssignableFrom(this.type) || OfflinePlayer.class.isAssignableFrom(this.type)) && FMLCommonHandler.instance().getSide() == Side.SERVER) {
				list.addAll(Arrays.asList(MinecraftServer.getServer().getAllUsernames()));
			}
			return list;
		}

	}

	@Getter
	@RequiredArgsConstructor
	public static final class StaticSubCommandParameter implements SubCommandParameter {

		private final String name;

		@Override
		public boolean test(final @NonNull String argument) {
			return this.name.equalsIgnoreCase(argument);
		}

		@Override
		public List<String> getTabComplete(final @NonNull CommandContext context) {
			return Arrays.asList(this.name);
		}

	}

	public static interface SubCommandParameter {

		String getName();

		boolean test(final @NonNull String argument);
		List<String> getTabComplete(final @NonNull CommandContext context);

	}

	@Getter
	public static class SubCommandScore implements Comparable<SubCommandScore> {

		public static final SubCommandScore ZERO = new SubCommandScore(0, 0);

		private final int score;
		private final int maxScore;
		private final float percentage;

		public SubCommandScore(final int score, final int maxScore) {
			this.score = score;
			this.maxScore = maxScore;
			this.percentage = (float) this.score / (float) this.maxScore;
		}

		@Override
		public int compareTo(final SubCommandScore o) {
			if (o == null) {
				return 1;
			}

			if (this.percentage == o.percentage) {
				if (this.score == o.score) {
					return Integer.compare(this.maxScore, o.maxScore);
				}

				return Integer.compare(this.score, o.score);
			}

			return Float.compare(this.percentage, o.percentage);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.maxScore, this.score);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof SubCommandScore)) {
				return false;
			}

			final SubCommandScore other = (SubCommandScore) obj;
			return this.maxScore == other.maxScore && this.score == other.score;
		}

	}

}