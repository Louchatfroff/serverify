// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.model.ServerEntry;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class LivePing implements AutoCloseable {
	private static final int TIMEOUT_SECONDS = 10;

	public record Result(boolean online, int players, int maxPlayers, String version, long pingMs, Component motd) {
	}

	private final ServerStatusPinger pinger = new ServerStatusPinger();

	public CompletableFuture<Result> ping(ServerEntry entry) {
		CompletableFuture<Result> done = new CompletableFuture<>();
		ServerData data = new ServerData(entry.name(), entry.address(), false);
		Util.backgroundExecutor().execute(() -> {
			try {
				pinger.pingServer(data, () -> Util.backgroundExecutor().execute(() -> {
					for (int i = 0; i < 30 && data.ping < 0; i++) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
					}
					done.complete(result(data));
				}));
			} catch (Exception e) {
				done.completeExceptionally(e);
			}
		});
		return done.orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	public void tick() {
		pinger.tick();
	}

	@Override
	public void close() {
		pinger.removeAll();
	}

	private static Result result(ServerData data) {
		boolean online = data.players != null;
		return new Result(online, online ? data.players.online() : 0, online ? data.players.max() : 0,
				data.version.getString(), data.ping, data.motd);
	}
}
