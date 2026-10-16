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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class TopMcServersSource implements ServerSource {
	private static final String BASE = "https://top-mc-servers.net";

	@Override
	public String id() {
		return "top-mc-servers.net";
	}

	@Override
	public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
		if (sort != Sort.POPULAR || (query != null && !query.trim().isEmpty())
				|| (category != null && category.topMcSlug() == null)) {
			return CompletableFuture.completedFuture(java.util.Collections.emptyList());
		}
		String url;
		if (category != null) {
			url = BASE + "/type/minecraft-" + category.topMcSlug() + "-servers" + (page <= 1 ? "" : "/" + page);
		} else {
			url = page <= 1 ? BASE + "/" : BASE + "/servers/" + page;
		}
		return Http.get(url, "text/html").thenApply(html -> parse(html, BASE + "/"));
	}

	static List<ServerEntry> parse(String html, String baseUri) {
		Document doc = Jsoup.parse(html, baseUri);
		List<ServerEntry> out = new ArrayList<>();
		for (Element card : doc.select("article.server-row-card")) {
			Element ipButton = card.selectFirst(".js-copy-ip-trigger[data-ip]");
			if (ipButton == null) {
				continue;
			}
			String address = ipButton.attr("data-ip").trim();
			String host = address;
			int port = ServerEntry.DEFAULT_PORT;
			int colon = address.lastIndexOf(':');
			if (colon > 0 && address.substring(colon + 1).matches("\\d{1,5}")) {
				host = address.substring(0, colon);
				port = Integer.parseInt(address.substring(colon + 1));
			}
			Set<String> tags = new LinkedHashSet<>();
			for (Element tag : card.select("a.tag-pill > span")) {
				tags.add(tag.text());
			}
			Element icon = card.selectFirst("img.server-icon-img");
			Element flag = card.selectFirst("img.flag-img");
			out.add(new ServerEntry(
					host,
					port,
					text(card, ".server-name"),
					text(card, ".server-desc-text"),
					card.selectFirst(".server-stats-status.is-online") != null,
					digits(text(card, ".stat-current")),
					digits(text(card, ".stat-max")),
					"",
					flag == null ? "" : flag.attr("alt"),
					digits(text(card, ".server-stats-metric-value--votes")),
					icon == null ? "" : icon.absUrl("src"),
					new ArrayList<>(tags),
					"top-mc-servers.net"));
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
