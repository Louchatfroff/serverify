// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.favorites;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serverify.model.ServerEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

public final class Favorites {
	private static final Logger LOG = Logger.getLogger("serverify");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path file;
	private final Map<String, ServerEntry> byKey = new LinkedHashMap<>();

	public Favorites(Path file) {
		this.file = file;
		try {
			if (Files.exists(file)) {
				ServerEntry[] saved = GSON.fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), ServerEntry[].class);
				if (saved != null) {
					for (ServerEntry e : saved) {
						byKey.put(e.key(), e);
					}
				}
			}
		} catch (IOException | RuntimeException e) {
			LOG.log(Level.WARNING, "Could not read favorites, starting empty: " + e);
		}
	}

	public synchronized List<ServerEntry> all() {
		return new ArrayList<>(byKey.values());
	}

	public synchronized boolean contains(ServerEntry entry) {
		return byKey.containsKey(entry.key());
	}

	public synchronized boolean toggle(ServerEntry entry) {
		boolean nowFavorite = byKey.remove(entry.key()) == null;
		if (nowFavorite) {
			byKey.put(entry.key(), entry);
		}
		save();
		return nowFavorite;
	}

	private void save() {
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, GSON.toJson(byKey.values()).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Could not save favorites: " + e);
		}
	}
}
