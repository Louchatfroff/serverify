// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.Serverify;
import com.serverify.legacy.BuildInfo;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "serverify", name = "Serverify", version = BuildInfo.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "*")
public final class ServerifyMod {
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Serverify.init(event.getModConfigurationDirectory().toPath());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
		//#if MC>=11202
		if (event.getGui() instanceof GuiMultiplayer) {
			event.getButtonList().add(ButtonFactory.create(event.getButtonList(), event.getGui().width));
		}
		//#else
		//$$ if (event.gui instanceof GuiMultiplayer) {
		//$$ 	event.buttonList.add(ButtonFactory.create(event.buttonList, event.gui.width));
		//$$ }
		//#endif
	}

	@SubscribeEvent
	public void onAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
		//#if MC>=11202
		if (event.getGui() instanceof GuiMultiplayer && event.getButton().id == ButtonFactory.ID) {
			Serverify.open();
		}
		//#else
		//$$ if (event.gui instanceof GuiMultiplayer && event.button.id == ButtonFactory.ID) {
		//$$ 	Serverify.open();
		//$$ }
		//#endif
	}
}
