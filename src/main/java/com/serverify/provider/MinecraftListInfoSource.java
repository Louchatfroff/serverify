// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.network.Http;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MinecraftListInfoSource implements ServerSource {
	private static final String BASE = "https://minecraft-list.info";

	@Override
	public String id() {
		return "minecraft-list.info";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		String order;
		switch (sort) {
			case POPULAR:
				order = "order%5BvotesCount%5D=desc";
				break;
			case NEWEST:
				order = "order%5BcreatedAt%5D=desc";
				break;
			case PLAYERS:
				order = "order%5Bplayers%5D=desc";
				break;
			default:
				order = "order%5Bname%5D=asc";
				break;
		}
		String url = BASE + "/api/v1/servers?page=" + page + "&" + order;
		String name = query == null ? "" : query.trim();
		if (category != null) {
			if (category.listInfoSlug() != null) {
				url += "&serverTypes.slug=" + category.listInfoSlug();
			} else if (name.isEmpty()) {
				name = category.nameTerm();
			}
		}
		if (!name.isEmpty()) {
			url += "&name=" + encode(name);
		}
		return Http.get(url, "application/ld+json").thenApply(MinecraftListInfoSource::parse);
	}

	static List<ServerEntry> parse(String json) {
		List<ServerEntry> out = new ArrayList<>();
		for (JsonElement element : new JsonParser().parse(json).getAsJsonObject().getAsJsonArray("member")) {
			JsonObject o = element.getAsJsonObject();
			String favicon = str(o, "faviconPath");
			out.add(new ServerEntry(
					str(o, "host"),
					o.has("port") && !o.get("port").isJsonNull() ? o.get("port").getAsInt() : ServerEntry.DEFAULT_PORT,
					str(o, "name"),
					str(o, "description"),
					o.has("online") && o.get("online").getAsBoolean(),
					num(o, "players"),
					num(o, "maxPlayers"),
					str(o, "serverVersionRaw"),
					str(o, "country"),
					num(o, "votesCount"),
					favicon.isEmpty() ? "" : BASE + favicon,
					java.util.Collections.emptyList(),
					"minecraft-list.info"));
		}
		return out;
	}

	private static String encode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String str(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
	}

	private static int num(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
	}
}
