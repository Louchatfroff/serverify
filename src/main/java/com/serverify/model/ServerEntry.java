// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.model;

import java.util.List;
import java.util.Locale;

public final class ServerEntry {
	public static final int DEFAULT_PORT = 25565;

	private final String host;
	private final int port;
	private final String name;
	private final String description;
	private final boolean online;
	private final int players;
	private final int maxPlayers;
	private final String version;
	private final String country;
	private final int votes;
	private final String iconUrl;
	private final List<String> tags;
	private final String source;

	public ServerEntry(String host, int port, String name, String description, boolean online, int players,
			int maxPlayers, String version, String country, int votes, String iconUrl, List<String> tags,
			String source) {
		this.host = host;
		this.port = port;
		this.name = name;
		this.description = description;
		this.online = online;
		this.players = players;
		this.maxPlayers = maxPlayers;
		this.version = version;
		this.country = country;
		this.votes = votes;
		this.iconUrl = iconUrl;
		this.tags = tags;
		this.source = source;
	}

	public String host() {
		return host;
	}

	public int port() {
		return port;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public boolean online() {
		return online;
	}

	public int players() {
		return players;
	}

	public int maxPlayers() {
		return maxPlayers;
	}

	public String version() {
		return version;
	}

	public String country() {
		return country;
	}

	public int votes() {
		return votes;
	}

	public String iconUrl() {
		return iconUrl;
	}

	public List<String> tags() {
		return tags;
	}

	public String source() {
		return source;
	}

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
