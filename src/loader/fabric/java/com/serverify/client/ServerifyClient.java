// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.Serverify;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

public final class ServerifyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Serverify.init(FabricLoader.getInstance().getConfigDir());
		ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> {
			if (screen instanceof JoinMultiplayerScreen) {
				ScreenWidgets.of(screen).add(ButtonFactory.create(width, Serverify::open));
			}
		});
	}
}
