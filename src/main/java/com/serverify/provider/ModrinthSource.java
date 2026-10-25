// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.network.Http;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ModrinthSource implements ServerSource {
	private static final String API = "https://api.modrinth.com";
	private static final int PAGE_SIZE = 20;

	@Override
	public String id() {
		return "modrinth";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		StringBuilder facets = new StringBuilder("[[\"project_type:minecraft_java_server\"]");
		if (category != null && category.modrinthSlug() != null) {
			facets.append(",[\"categories:").append(category.modrinthSlug()).append("\"]");
		}
		facets.append("]");
		String index = sort == Sort.NEWEST ? "newest" : sort == Sort.NAME ? "relevance" : "downloads";
		String url = API + "/v2/search?limit=" + PAGE_SIZE + "&offset=" + Math.max(0, page - 1) * PAGE_SIZE
				+ "&index=" + index + "&facets=" + encode(facets.toString());
		String text = query == null ? "" : query.trim();
		if (!text.isEmpty()) {
			url += "&query=" + encode(text);
		}
		return Http.get(url, "application/json").thenCompose(search -> {
			JsonArray hits = new JsonParser().parse(search).getAsJsonObject().getAsJsonArray("hits");
			if (hits.size() == 0) {
				return CompletableFuture.completedFuture(new ArrayList<ServerEntry>());
			}
			JsonArray ids = new JsonArray();
			for (JsonElement hit : hits) {
				ids.add(hit.getAsJsonObject().get("project_id"));
			}
			return Http.get(API + "/v3/projects?ids=" + encode(ids.toString()), "application/json")
					.thenApply(projects -> parse(search, projects));
		});
	}

	static List<ServerEntry> parse(String searchJson, String projectsJson) {
		Map<String, JsonObject> projects = new HashMap<>();
		for (JsonElement element : new JsonParser().parse(projectsJson).getAsJsonArray()) {
			JsonObject project = element.getAsJsonObject();
			projects.put(project.get("id").getAsString(), project);
		}
		List<ServerEntry> out = new ArrayList<>();
		for (JsonElement element : new JsonParser().parse(searchJson).getAsJsonObject().getAsJsonArray("hits")) {
			JsonObject hit = element.getAsJsonObject();
			JsonObject project = projects.get(str(hit, "project_id"));
			JsonObject java = project == null ? null : obj(project, "minecraft_java_server");
			String address = java == null ? "" : str(java, "address").trim();
			if (address.isEmpty()) {
				continue;
			}
			String host = address;
			int port = ServerEntry.DEFAULT_PORT;
			int colon = address.lastIndexOf(':');
			if (colon > 0 && address.substring(colon + 1).matches("\\d{1,5}")) {
				host = address.substring(0, colon);
				port = Integer.parseInt(address.substring(colon + 1));
			}
			JsonObject ping = obj(obj(java, "ping"), "data");
			JsonObject meta = obj(project, "minecraft_server");
			List<String> tags = new ArrayList<>();
			JsonArray categories = hit.has("display_categories") ? hit.getAsJsonArray("display_categories") : new JsonArray();
			for (JsonElement category : categories) {
				tags.add(category.getAsString());
			}
			out.add(new ServerEntry(
					host,
					port,
					str(hit, "title"),
					str(hit, "description"),
					ping != null,
					ping == null ? 0 : num(ping, "players_online"),
					ping == null ? 0 : num(ping, "players_max"),
					ping == null ? "" : str(ping, "version_name"),
					meta == null ? "" : str(meta, "country"),
					num(hit, "follows"),
					str(hit, "icon_url"),
					tags,
					"modrinth"));
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

	private static JsonObject obj(JsonObject o, String key) {
		return o != null && o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : null;
	}

	private static String str(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
	}

	private static int num(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : 0;
	}
}
