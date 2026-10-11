// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.model;

import java.util.List;
import java.util.Locale;

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

	public String key() {
		String h = host.toLowerCase(Locale.ROOT);
		if (h.endsWith(".")) {
			h = h.substring(0, h.length() - 1);
		}
		return h + ":" + port;
	}

	public String address() {
		return port == DEFAULT_PORT ? host : host + ":" + port;
	}

	public ServerEntry withStatus(boolean online, int players, int maxPlayers, String version) {
		return new ServerEntry(host, port, name, description, online, players, maxPlayers, version, country, votes,
				iconUrl, tags, source);
	}
}
