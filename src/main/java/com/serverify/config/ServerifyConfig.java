// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerifyConfig {
	public static final int MIN_REFRESH_SECONDS = 30;
	public static final int MAX_REFRESH_SECONDS = 60;
	private static final System.Logger LOG = System.getLogger("serverify");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public boolean minecraftListInfoEnabled = true;
	public boolean topMcServersEnabled = true;
	public int refreshSeconds = 45;

	private transient Path file;

	public static ServerifyConfig load(Path file) {
		ServerifyConfig config = new ServerifyConfig();
		try {
			if (Files.exists(file)) {
				ServerifyConfig read = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), ServerifyConfig.class);
				if (read != null) {
					config = read;
				}
			}
		} catch (IOException | RuntimeException e) {
			LOG.log(System.Logger.Level.WARNING, "Could not read config, using defaults: " + e);
		}
		config.file = file;
		config.refreshSeconds = Math.max(MIN_REFRESH_SECONDS, Math.min(MAX_REFRESH_SECONDS, config.refreshSeconds));
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOG.log(System.Logger.Level.WARNING, "Could not save config: " + e);
		}
	}
}
