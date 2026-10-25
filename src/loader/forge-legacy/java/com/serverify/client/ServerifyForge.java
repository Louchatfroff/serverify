// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.Serverify;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("serverify")
public final class ServerifyForge {
	public ServerifyForge() {
		Serverify.init(FMLPaths.CONFIGDIR.get());
		MinecraftForge.EVENT_BUS.addListener(ServerifyForge::onScreenInit);
	}

	private static void onScreenInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof JoinMultiplayerScreen) {
			event.addWidget(ButtonFactory.create(event.getGui(), event.getGui().width, Serverify::open));
		}
	}
}
