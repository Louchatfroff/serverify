// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.Region;
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

public final class MinecraftMpSource implements ServerSource {
	private static final String BASE = "https://minecraft-mp.com";
	private static final Set<String> TYPES = new HashSet<>(Arrays.asList("bedwars", "factions", "lifesteal", "oneblock",
			"pixelmon", "prison", "pvp", "roleplay", "skyblock", "survival", "towny", "vanilla"));

	@Override
	public String id() {
		return "minecraft-mp.com";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		if (sort != Sort.POPULAR || (query != null && !query.trim().isEmpty())
				|| (category != null && !TYPES.contains(category.nameTerm()))) {
			return CompletableFuture.completedFuture(Collections.<ServerEntry>emptyList());
		}
		String path = category == null ? "/servers/list/" : "/type/" + (category == Category.FACTIONS ? "faction" : category.nameTerm()) + "/";
		String url = category == null && page <= 1 ? BASE + "/" : BASE + path + (page <= 1 ? "" : page + "/");
		return Http.get(url, "text/html").thenApply(html -> parse(html, BASE + "/"));
	}

	static List<ServerEntry> parse(String html, String baseUri) {
		Document doc = Jsoup.parse(html, baseUri);
		List<ServerEntry> out = new ArrayList<>();
		for (Element row : doc.select("tr")) {
			Element address = row.selectFirst("button.clipboard[data-clipboard-text]");
			Element name = row.selectFirst(".server-title-responsive a");
			Element first = row.selectFirst("td");
			if (address == null || name == null || first == null || first.selectFirst("img[alt=Premium server]") != null) {
				continue;
			}
			String host = address.attr("data-clipboard-text").trim();
			int port = ServerEntry.DEFAULT_PORT;
			int colon = host.lastIndexOf(':');
			if (colon > 0 && host.substring(colon + 1).matches("\\d{1,5}")) {
				port = Integer.parseInt(host.substring(colon + 1));
				host = host.substring(0, colon);
			}
			Element flag = row.selectFirst("img[src*=/flags/]");
			Element icon = first.selectFirst("img[src*=/favicon/]");
			Element players = row.selectFirst("td.hidden-xs .fa-users");
			List<String> tags = new ArrayList<>();
			for (Element tag : row.select(".btn-tag")) {
				tags.add(tag.text());
			}
			out.add(new ServerEntry(
					host,
					port,
					name.text().trim(),
					text(row, ".server-description"),
					row.selectFirst("button.btn-success") != null && "Online".equals(row.selectFirst("button.btn-success").text().trim()),
					players == null || players.parent().selectFirst("strong") == null ? 0 : digits(players.parent().selectFirst("strong").text()),
					players == null || players.parent().selectFirst("small") == null ? 0 : digits(players.parent().selectFirst("small").text()),
					"",
					flag == null ? "" : Region.codeOf(flag.attr("alt")),
					0,
					icon == null ? "" : icon.absUrl("src"),
					tags,
					"minecraft-mp.com"));
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
