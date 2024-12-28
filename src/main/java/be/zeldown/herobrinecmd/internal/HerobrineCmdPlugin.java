package be.zeldown.herobrinecmd.internal;

import org.bukkit.plugin.java.JavaPlugin;

import be.zeldown.herobrinecmd.lib.command.registry.CommandRegistry;

public class HerobrineCmdPlugin extends JavaPlugin {

	private static HerobrineCmdPlugin instance;

	@Override
	public void onEnable() {
		HerobrineCmdPlugin.instance = this;
		CommandRegistry.register();
	}

	/* Instance */
	public static HerobrineCmdPlugin getInstance() {
		return HerobrineCmdPlugin.instance;
	}

}