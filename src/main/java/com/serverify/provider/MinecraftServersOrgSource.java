// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.network.Http;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MinecraftServersOrgSource implements ServerSource {
	private static final String BASE = "https://minecraftservers.org";
	private static final Set<String> TYPES = new HashSet<>(Arrays.asList("anarchy", "bedwars", "cobblemon", "creative",
			"earth", "economy", "factions", "kitpvp", "lifesteal", "modded", "oneblock", "parkour", "pixelmon", "prison",
			"pvp", "roleplay", "rpg", "skyblock", "smp", "survival", "towny", "vanilla"));

	@Override
	public String id() {
		return "minecraftservers.org";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		if (sort != Sort.POPULAR || (query != null && !query.trim().isEmpty())
				|| (category != null && !TYPES.contains(category.nameTerm()))) {
			return CompletableFuture.completedFuture(Collections.<ServerEntry>emptyList());
		}
		String url = category == null ? BASE + "/index/" + page : BASE + "/type/" + category.nameTerm() + (page <= 1 ? "" : "/" + page);
		return Http.get(url, "text/html").thenApply(html -> parse(html, BASE + "/"));
	}

	static List<ServerEntry> parse(String html, String baseUri) {
		Document doc = Jsoup.parse(html, baseUri);
		List<ServerEntry> out = new ArrayList<>();
		for (Element card : doc.select("div.server-listing:not(.highlight)")) {
			Element address = card.selectFirst(".copy[data-clipboard-content]");
			Element name = card.selectFirst(".name a");
			if (address == null || name == null) {
				continue;
			}
			String host = address.attr("data-clipboard-content").trim();
			int port = ServerEntry.DEFAULT_PORT;
			int colon = host.lastIndexOf(':');
			if (colon > 0 && host.substring(colon + 1).matches("\\d{1,5}")) {
				port = Integer.parseInt(host.substring(colon + 1));
				host = host.substring(0, colon);
			}
			String[] players = text(card, ".players .value").split("/");
			String id = card.attr("data-id");
			out.add(new ServerEntry(
					host,
					port,
					name.text().trim(),
					"",
					card.selectFirst(".status .value.online") != null,
					digits(players[0]),
					players.length > 1 ? digits(players[1]) : 0,
					"",
					"",
					0,
					id.isEmpty() ? "" : "https://status.minecraftservers.org/icon/" + id + ".png",
					Collections.<String>emptyList(),
					"minecraftservers.org"));
		}
		return out;
	}

	private static String text(Element root, String selector) {
		Element e = root.selectFirst(selector);
		return e == null ? "" : e.text().trim();
	}

	private static int digits(String s) {
		String d = s.replaceAll("\\D", "");
		return d.isEmpty() ? 0 : Integer.parseInt(d);
	}
}
