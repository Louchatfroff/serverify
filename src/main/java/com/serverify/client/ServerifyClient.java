// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.config.ServerifyConfig;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.favorites.Favorites;
import com.serverify.provider.MinecraftListInfoSource;
import com.serverify.provider.ServerSource;
import com.serverify.provider.ServerSourceRegistry;
import com.serverify.provider.TopMcServersSource;
import gg.essential.universal.UScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ServerifyClient implements ClientModInitializer {
	public static Favorites favorites;
	public static ServerifyConfig config;

	/**
	 * Enabled sources in load order: minecraft-list.info first, so it loads first and wins when a
	 * scraped site lists the same server.
	 */
	public static ServerSourceRegistry sources() {
		List<ServerSource> enabled = new ArrayList<>();
		if (config.minecraftListInfoEnabled) {
			enabled.add(new MinecraftListInfoSource());
		}
		if (config.topMcServersEnabled) {
			enabled.add(new TopMcServersSource());
		}
		return new ServerSourceRegistry(enabled);
	}

	@Override
	public void onInitializeClient() {
		Path dir = FabricLoader.getInstance().getConfigDir();
		config = ServerifyConfig.load(dir.resolve("serverify.json"));
		favorites = new Favorites(dir.resolve("serverify").resolve("favorites.json"));
		ScreenEvents.AFTER_INIT.register((minecraft, screen, width, height) -> {
			if (screen instanceof JoinMultiplayerScreen) {
				Screens.getWidgets(screen).add(Button
						.builder(Component.translatable("serverify.open"), button -> UScreen.displayScreen(new ServerBrowserScreen()))
						.bounds(width - 108, 6, 100, 20)
						.build());
			}
		});
	}
}
