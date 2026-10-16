// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.Serverify;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("serverify")
public final class ServerifyNeoForge {
	public ServerifyNeoForge() {
		Serverify.init(FMLPaths.CONFIGDIR.get());
		NeoForge.EVENT_BUS.addListener(ServerifyNeoForge::onScreenInit);
	}

	private static void onScreenInit(ScreenEvent.Init.Post event) {
		if (event.getScreen() instanceof JoinMultiplayerScreen) {
			event.addListener(ButtonFactory.create(event.getScreen().width, Serverify::open));
		}
	}
}
