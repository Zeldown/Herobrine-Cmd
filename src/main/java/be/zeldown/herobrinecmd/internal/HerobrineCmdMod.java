package be.zeldown.herobrinecmd.internal;

import be.zeldown.herobrinecmd.internal.common.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Constants.MOD_ID, version = Constants.VERSION, acceptableRemoteVersions = "*")
public class HerobrineCmdMod {

	@Instance(Constants.MOD_ID)
	private static HerobrineCmdMod instance;

	@SidedProxy(clientSide = "be.zeldown.herobrinecmd.internal.client.ClientProxy", serverSide = "be.zeldown.herobrinecmd.internal.server.ServerProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {
		HerobrineCmdMod.proxy.onPreInit(event);
	}

	@EventHandler
	public void onInit(final FMLInitializationEvent event) {
		HerobrineCmdMod.proxy.onInit(event);
	}

	@EventHandler
	public void onPostInit(final FMLPostInitializationEvent event) {
		HerobrineCmdMod.proxy.onPostInit(event);
	}

	@EventHandler
	public void onServerStarting(final FMLServerStartingEvent event) {
		HerobrineCmdMod.proxy.onServerStarting(event);
	}

	@EventHandler
	public void onServerStarted(final FMLServerStartedEvent event) {
		HerobrineCmdMod.proxy.onServerStarted(event);
	}

	/* Instance */
	public static HerobrineCmdMod getInstance() {
		return HerobrineCmdMod.instance;
	}

}
