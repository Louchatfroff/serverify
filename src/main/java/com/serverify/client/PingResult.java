// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;


public final class PingResult {
	private final boolean online;
	private final int players;
	private final int maxPlayers;
	private final String version;
	private final long pingMs;
	private final String motd;

	public PingResult(boolean online, int players, int maxPlayers, String version, long pingMs, String motd) {
		this.online = online;
		this.players = players;
		this.maxPlayers = maxPlayers;
		this.version = version;
		this.pingMs = pingMs;
		this.motd = motd;
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

	public long pingMs() {
		return pingMs;
	}

	public String motd() {
		return motd;
	}
}
