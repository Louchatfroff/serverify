// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify;

import com.serverify.config.ServerifyConfig;
import com.serverify.favorites.Favorites;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.provider.MinecraftListInfoSource;
import com.serverify.provider.ServerSource;
import com.serverify.provider.ServerSourceRegistry;
import com.serverify.provider.TopMcServersSource;
import gg.essential.universal.UScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Serverify {
	public static Favorites favorites;
	public static ServerifyConfig config;

	private Serverify() {
	}

	public static void init(Path configDir) {
		config = ServerifyConfig.load(configDir.resolve("serverify.json"));
		favorites = new Favorites(configDir.resolve("serverify").resolve("favorites.json"));
		if (Boolean.getBoolean("serverify.smoke")) {
			try {
				Class.forName("com.serverify.smoke.Smoke").getMethod("start").invoke(null);
			} catch (ReflectiveOperationException ignored) {
			}
		}
	}

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

	public static void open() {
		UScreen.displayScreen(new ServerBrowserScreen());
	}
}
