package be.zeldown.herobrinecmd.internal.common.utils;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public interface IProxy {

	void onPreInit(final FMLPreInitializationEvent event);
	void onInit(final FMLInitializationEvent event);
	void onPostInit(final FMLPostInitializationEvent event);
	void onServerStarting(final FMLServerStartingEvent event);
	void onServerStarted(final FMLServerStartedEvent event);

}