// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.Serverify;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("serverify")
public final class ServerifyForge {
	public ServerifyForge() {
		Serverify.init(FMLPaths.CONFIGDIR.get());
		MinecraftForge.EVENT_BUS.addListener(ServerifyForge::onScreenInit);
	}

	private static void onScreenInit(ScreenEvent.Init.Post event) {
		if (event.getScreen() instanceof JoinMultiplayerScreen) {
			event.addListener(ButtonFactory.create(event.getScreen(), event.getScreen().width, Serverify::open));
		}
	}
}
