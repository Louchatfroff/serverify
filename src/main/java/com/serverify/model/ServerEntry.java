package com.serverify.model;

import java.util.List;
import java.util.Locale;

/** One public server as shown on a browser card. Immutable. */
public record ServerEntry(
		String host,
		int port,
		String name,
		String description,
		boolean online,
		int players,
		int maxPlayers,
		String version,
		String country,
		int votes,
		String iconUrl,
		List<String> tags,
		String source) {

	public static final int DEFAULT_PORT = 25565;

	/** Dedupe identity: lowercase host without trailing dot, plus port. */
	public String key() {
		String h = host.toLowerCase(Locale.ROOT);
		if (h.endsWith(".")) {
			h = h.substring(0, h.length() - 1);
		}
		return h + ":" + port;
	}

	/** Address as typed into Minecraft's server field (port omitted when default). */
	public String address() {
		return port == DEFAULT_PORT ? host : host + ":" + port;
	}

	public ServerEntry withStatus(boolean online, int players, int maxPlayers, String version) {
		return new ServerEntry(host, port, name, description, online, players, maxPlayers, version, country, votes,
				iconUrl, tags, source);
	}
}
