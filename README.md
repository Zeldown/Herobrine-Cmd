<div align="center">

# Herobrine-Cmd

<div align="center" >
  <img align="center" src="https://img.shields.io/badge/version-1.0.0 (a409981)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://github.com/Zeldown/Herobrine-Cmd/actions/workflows/release.yml/badge.svg">
</div>

<br>

A powerful annotation-based command system for Forge modding.

Elevate your Minecraft mods with ForgeCommandFramework, designed to simplify and streamline command creation.

ForgeCommandFramework empowers developers by providing a highly customizable, annotation-driven API for defining commands, arguments, and subcommands—all without tedious boilerplate code.
</div>

# Example
```java
@Command(command = {"/money", "/balance"}, description = "Manage your balance", permission = "command.money.use", sender = SenderType.ALL)
public class AnnotatedCommandTestMoney {

	@SubCommand(command = "/money", description = "Show your balance", sender = SenderType.PLAYER)
	public void moneyCommand(final CommandContext context) {
		context.send("You have " + 1000 + "$");
	}

	@SubCommand(command = "/money top", description = "Show the leaderboard")
	public void moneyCommandTop(final CommandContext context) {
		context.send("Leaderboard :");
		context.send("1. " + "Player1" + " : " + 1000 + "$");
		context.send("2. " + "Player2" + " : " + 500 + "$");
		context.send("3. " + "Player3" + " : " + 250 + "$");
	}

	@SubCommand(command = "/money <player>", description = "Show player's balance", permission = "command.money.admin")
	public void moneyCommandPlayer(final CommandContext context, final OfflinePlayer player) {
		context.send(player.getName() + " has " + 1000 + "$");
	}

	@SubCommand(command = "/money give <player> <amount>", description = "Give money to player", permission = "command.money.admin")
	public void moneyCommandGive(final CommandContext context, final OfflinePlayer player, final double amount) {
		context.send("You gave " + amount + "$ to " + player.getName());
	}

	@SubCommand(command = "/money set <player> <amount>", description = "Set player's balance", permission = "command.money.admin")
	public void moneyCommandSet(final CommandContext context, final OfflinePlayer player, final double amount) {
		context.send("You set " + player.getName() + " to " + amount + "$");
	}

}
```
