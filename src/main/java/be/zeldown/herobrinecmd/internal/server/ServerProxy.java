package be.zeldown.herobrinecmd.internal.server;

import be.zeldown.herobrinecmd.internal.common.CommonProxy;
import be.zeldown.herobrinecmd.lib.command.registry.CommandRegistry;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ServerProxy extends CommonProxy {

	@Override
	public void onServerStarting(final FMLServerStartingEvent event) {
		CommandRegistry.register();
	}

}