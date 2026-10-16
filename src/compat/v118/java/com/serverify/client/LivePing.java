// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.model.ServerEntry;
import com.serverify.network.Async;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LivePing implements AutoCloseable {
	private static final int TIMEOUT_SECONDS = 10;

	private final ServerStatusPinger pinger = new ServerStatusPinger();

	public CompletableFuture<PingResult> ping(ServerEntry entry) {
		CompletableFuture<PingResult> done = new CompletableFuture<>();
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
		return Async.timeout(done, TIMEOUT_SECONDS);
	}

	public void tick() {
		pinger.tick();
	}

	@Override
	public void close() {
		pinger.removeAll();
	}

	private static final Pattern PLAYERS = Pattern.compile("(\\d+)/(\\d+)");

	private static PingResult result(ServerData data) {
		Matcher matcher = PLAYERS.matcher(data.status.getString());
		boolean online = matcher.find();
		return new PingResult(online, online ? Integer.parseInt(matcher.group(1)) : 0, online ? Integer.parseInt(matcher.group(2)) : 0,
				data.version.getString(), data.ping, data.motd);
	}
}
