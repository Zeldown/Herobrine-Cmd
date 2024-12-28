package be.zeldown.herobrinecmd.lib.command.test.admin;

import be.zeldown.herobrinecmd.lib.command.annotation.SubCommand;
import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.entity.OfflinePlayer;
import net.minecraft.entity.player.EntityPlayer;

public class AnnotatedCommandTestMoneyAdmin {

	@SubCommand(command = "/money <player>", description = "Affiche l'argent d'un joueur")
	public void moneyCommandPlayer(final CommandContext context, final OfflinePlayer player) {
		context.send("Le joueur " + player.getName() + " a actuellement " + 1000 + "$");
	}

	@SubCommand(command = "/money give <player> <amount>", description = "Donne de l'argent à un joueur")
	public void moneyCommandGive(final CommandContext context, final EntityPlayer player, final double amount) {
		context.send("Vous avez donné " + amount + "$ à " + player.getCommandSenderName());
	}

	@SubCommand(command = "/money set <player> <amount>", description = "Définit l'argent d'un joueur")
	public void moneyCommandSet(final CommandContext context, final EntityPlayer player, final double amount) {
		context.send("Vous avez défini l'argent de " + player.getCommandSenderName() + " à " + amount + "$");
	}

}