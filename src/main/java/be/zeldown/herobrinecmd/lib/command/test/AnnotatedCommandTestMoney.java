package be.zeldown.herobrinecmd.lib.command.test;

import be.zeldown.herobrinecmd.lib.SenderType;
import be.zeldown.herobrinecmd.lib.command.annotation.Command;
import be.zeldown.herobrinecmd.lib.command.annotation.SubCommand;
import be.zeldown.herobrinecmd.lib.command.annotation.SubCommandClass;
import be.zeldown.herobrinecmd.lib.command.context.CommandContext;
import be.zeldown.herobrinecmd.lib.command.test.admin.AnnotatedCommandTestMoneyAdmin;

@Command(command = {"/money", "/balance"}, description = "Gère le système d'argent", sender = SenderType.ALL)
public class AnnotatedCommandTestMoney {

	@SubCommandClass
	private AnnotatedCommandTestMoneyAdmin admin;

	@SubCommand(command = "/money", description = "Affiche votre argent", sender = SenderType.PLAYER)
	public void moneyCommand(final CommandContext context) {
		context.send("Vous avez actuellement " + 1000 + "$");
	}

	@SubCommand(command = "/money top", description = "Affiche le classement", priority = 1)
	public void moneyCommandTop(final CommandContext context) {
		context.send("Classement des joueurs les plus riches :");
		context.send("1. " + "Player1" + " : " + 1000 + "$");
		context.send("2. " + "Player2" + " : " + 500 + "$");
		context.send("3. " + "Player3" + " : " + 250 + "$");
	}

}