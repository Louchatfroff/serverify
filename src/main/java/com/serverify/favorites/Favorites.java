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
import java.util.Map;

/** Favorite servers, kept in a small JSON file and written on every change. */
public final class Favorites {
	private static final System.Logger LOG = System.getLogger("serverify");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path file;
	private final Map<String, ServerEntry> byKey = new LinkedHashMap<>();

	public Favorites(Path file) {
		this.file = file;
		try {
			if (Files.exists(file)) {
				ServerEntry[] saved = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), ServerEntry[].class);
				if (saved != null) {
					for (ServerEntry e : saved) {
						byKey.put(e.key(), e);
					}
				}
			}
		} catch (IOException | RuntimeException e) {
			LOG.log(System.Logger.Level.WARNING, "Could not read favorites, starting empty: " + e);
		}
	}

	public synchronized List<ServerEntry> all() {
		return new ArrayList<>(byKey.values());
	}

	public synchronized boolean contains(ServerEntry entry) {
		return byKey.containsKey(entry.key());
	}

	/** @return true if the server is a favorite after the call */
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
			Files.writeString(file, GSON.toJson(byKey.values()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOG.log(System.Logger.Level.WARNING, "Could not save favorites: " + e);
		}
	}
}
