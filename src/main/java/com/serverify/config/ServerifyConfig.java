// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serverify.model.Region;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerifyConfig {
	public static final int MIN_REFRESH_SECONDS = 30;
	public static final int MAX_REFRESH_SECONDS = 60;
	private static final Logger LOG = Logger.getLogger("serverify");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public boolean modrinthEnabled = true;
	public boolean minecraftListInfoEnabled = true;
	public boolean topMcServersEnabled = true;
	public boolean moreSourcesEnabled = true;
	public String region = "auto";
	public int refreshSeconds = 45;

	private transient Path file;

	public static ServerifyConfig load(Path file) {
		ServerifyConfig config = new ServerifyConfig();
		try {
			if (Files.exists(file)) {
				ServerifyConfig read = GSON.fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), ServerifyConfig.class);
				if (read != null) {
					config = read;
				}
			}
		} catch (IOException | RuntimeException e) {
			LOG.log(Level.WARNING, "Could not read config, using defaults: " + e);
		}
		config.file = file;
		config.refreshSeconds = Math.max(MIN_REFRESH_SECONDS, Math.min(MAX_REFRESH_SECONDS, config.refreshSeconds));
		return config;
	}

	public Region regionFilter() {
		if ("all".equals(region)) {
			return null;
		}
		try {
			return Region.valueOf(region);
		} catch (IllegalArgumentException | NullPointerException e) {
			return Region.local();
		}
	}

	public void save() {
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Could not save config: " + e);
		}
	}
}
