// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.network.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.serverify.model.Category.*;

public final class FeaturedSource implements ServerSource {
	private static final String STATUS = "https://api.mcstatus.io/v2/status/java/";

	private static final class Featured {
		final String name;
		final String host;
		final String description;
		final Set<Category> categories;

		Featured(String name, String host, String description, Category... categories) {
			this.name = name;
			this.host = host;
			this.description = description;
			this.categories = EnumSet.copyOf(Arrays.asList(categories));
		}
	}

	private static final List<Featured> SERVERS = Arrays.asList(
			new Featured("Hypixel Network", "mc.hypixel.net", "The largest server: Bed Wars, SkyWars, SkyBlock and many more games.", MINIGAMES, BEDWARS, SKYWARS, SKYBLOCK, PVP),
			new Featured("CubeCraft Games", "play.cubecraft.net", "Long running minigame network with Bed Wars, SkyWars and Egg Wars.", MINIGAMES, BEDWARS, SKYWARS),
			new Featured("PikaNetwork", "play.pika-network.net", "Bed Wars, SkyWars, KitPvP, SkyBlock and more on one network.", MINIGAMES, BEDWARS, SKYWARS, KITPVP, SKYBLOCK),
			new Featured("MCC Island", "play.mccisland.net", "The official Minecraft Championship minigame server.", MINIGAMES),
			new Featured("JartexNetwork", "play.jartexnetwork.com", "Survival, SkyBlock, Prison and Factions on a big network.", SURVIVAL, SKYBLOCK, PRISON, FACTIONS),
			new Featured("Manacube", "play.manacube.com", "SkyBlock, Prison, Survival and Creative with daily updates.", SKYBLOCK, PRISON, SURVIVAL, CREATIVE),
			new Featured("OPBlocks", "play.opblocks.com", "SkyBlock, Survival and Prison.", SKYBLOCK, SURVIVAL, PRISON),
			new Featured("Mineberry", "play.mineberry.org", "A survival network for 1.8 up to the newest version.", SURVIVAL),
			new Featured("Purple Prison", "play.purpleprison.net", "One of the oldest and best known prison servers.", PRISON),
			new Featured("SaiCoPvP", "play.saicopvp.com", "Factions and PvP with custom content.", FACTIONS, PVP),
			new Featured("PvP Legacy", "play.pvplegacy.net", "Competitive PvP with custom modes.", PVP),
			new Featured("Lifesteal Network", "lifesteal.net", "Lifesteal survival where every kill matters.", LIFESTEAL, PVP),
			new Featured("StrongCraft", "play.strongcraft.org", "LifeSteal, BoxPvP and Economy modes.", LIFESTEAL),
			new Featured("DonutSMP", "donutsmp.net", "A huge economy survival SMP.", SMP, SURVIVAL, ECONOMY),
			new Featured("EarthMC", "play.earthmc.net", "Build nations and towns on a map of the real Earth.", EARTH, TOWNY, ECONOMY),
			new Featured("Wynncraft", "play.wynncraft.com", "A full MMORPG with quests, dungeons and raids.", RPG),
			new Featured("2b2t", "2b2t.org", "The oldest anarchy server with no rules.", ANARCHY),
			new Featured("Complex Gaming", "play.mc-complex.com", "Pixelmon with gyms, legendaries and ranked battles.", PIXELMON, MODDED),
			new Featured("Complex Cobblemon", "cobblemon.mc-complex.com", "Cobblemon with gyms, raids and every Mega Evolution.", COBBLEMON)
	);

	@Override
	public String id() {
		return "featured";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		if (category == null || page > 1 || (query != null && !query.trim().isEmpty())) {
			return CompletableFuture.completedFuture(Collections.<ServerEntry>emptyList());
		}
		List<CompletableFuture<ServerEntry>> pending = new ArrayList<>();
		for (Featured server : SERVERS) {
			if (server.categories.contains(category)) {
				pending.add(status(server));
			}
		}
		return CompletableFuture.allOf(pending.toArray(new CompletableFuture[0])).thenApply(ignored -> {
			List<ServerEntry> out = new ArrayList<>();
			for (CompletableFuture<ServerEntry> entry : pending) {
				out.add(entry.join());
			}
			return out;
		});
	}

	private static CompletableFuture<ServerEntry> status(Featured server) {
		return Http.get(STATUS + server.host, "application/json")
				.thenApply(json -> parse(server, json))
				.exceptionally(error -> entry(server, false, 0, 0, "", ""));
	}

	private static ServerEntry parse(Featured server, String json) {
		JsonObject root = new JsonParser().parse(json).getAsJsonObject();
		boolean online = root.has("online") && root.get("online").getAsBoolean();
		JsonObject players = root.has("players") && root.get("players").isJsonObject() ? root.getAsJsonObject("players") : null;
		JsonObject version = root.has("version") && root.get("version").isJsonObject() ? root.getAsJsonObject("version") : null;
		return entry(server, online,
				players == null ? 0 : integer(players, "online"),
				players == null ? 0 : integer(players, "max"),
				version == null ? "" : string(version, "name_clean"),
				string(root, "icon"));
	}

	private static ServerEntry entry(Featured server, boolean online, int players, int max, String version, String icon) {
		return new ServerEntry(server.host, ServerEntry.DEFAULT_PORT, server.name, server.description, online, players,
				max, version, "", 0, icon, Collections.<String>emptyList(), "featured");
	}

	private static String string(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e == null || e.isJsonNull() ? "" : e.getAsString();
	}

	private static int integer(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e == null || e.isJsonNull() ? 0 : e.getAsInt();
	}

	static int count(Category category) {
		int n = 0;
		for (Featured server : SERVERS) {
			if (server.categories.contains(category)) {
				n++;
			}
		}
		return n;
	}
}
