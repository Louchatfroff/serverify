// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.model.ServerEntry;
import com.serverify.network.Async;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.client.FMLClientHandler;
//#if MC>=11202
import net.minecraft.client.network.ServerPinger;
//#else
//$$ import net.minecraft.client.network.OldServerPinger;
//#endif

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LivePing implements AutoCloseable {
	private static final int TIMEOUT_SECONDS = 10;
	private static final Pattern PLAYERS = Pattern.compile("(\\d+)/(\\d+)");

	//#if MC>=11202
	private final ServerPinger pinger = new ServerPinger();
	//#else
	//$$ private final OldServerPinger pinger = new OldServerPinger();
	//#endif
	private volatile ServerData data;
	private volatile CompletableFuture<PingResult> pending;

	public LivePing() {
		FMLClientHandler.instance().setupServerList();
	}

	public CompletableFuture<PingResult> ping(ServerEntry entry) {
		CompletableFuture<PingResult> done = new CompletableFuture<>();
		ServerData server = new ServerData(entry.name(), entry.address(), false);
		server.pinged = true;
		server.pingToServer = -2L;
		server.serverMOTD = "";
		server.populationInfo = "";
		data = server;
		pending = done;
		CompletableFuture.runAsync(() -> {
			try {
				pinger.ping(server);
			} catch (Exception e) {
				done.completeExceptionally(e);
			}
		});
		return Async.timeout(done, TIMEOUT_SECONDS);
	}

	public void tick() {
		pinger.pingPendingNetworks();
		ServerData server = data;
		CompletableFuture<PingResult> done = pending;
		if (server != null && done != null && !done.isDone() && server.pingToServer >= 0) {
			done.complete(result(server));
		}
	}

	@Override
	public void close() {
		pinger.clearPendingNetworks();
	}

	private static PingResult result(ServerData server) {
		String population = server.populationInfo == null ? "" : server.populationInfo.replaceAll("§.", "");
		Matcher matcher = PLAYERS.matcher(population);
		boolean online = matcher.find();
		return new PingResult(online, online ? Integer.parseInt(matcher.group(1)) : 0, online ? Integer.parseInt(matcher.group(2)) : 0,
				server.gameVersion == null ? "" : server.gameVersion, server.pingToServer, server.serverMOTD == null ? "" : server.serverMOTD);
	}
}
