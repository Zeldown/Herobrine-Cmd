package be.zeldown.herobrinecmd.internal.common;

import be.zeldown.herobrinecmd.internal.common.utils.IProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public abstract class CommonProxy implements IProxy {

	@Override
	public void onPreInit(final FMLPreInitializationEvent event) {}

	@Override
	public void onInit(final FMLInitializationEvent event) {}

	@Override
	public void onPostInit(final FMLPostInitializationEvent event) {}

	@Override
	public void onServerStarting(final FMLServerStartingEvent event) {}

	@Override
	public void onServerStarted(final FMLServerStartedEvent event) {}

}