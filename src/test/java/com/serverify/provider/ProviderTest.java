// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.provider.ServerSource.Sort;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderTest {

	private static String resource(String name) throws IOException {
		try (var in = ProviderTest.class.getResourceAsStream("/" + name)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static ServerEntry entry(String host, int port, String source) {
		return new ServerEntry(host, port, host, "", true, 1, 10, "", "", 0, "", List.of(), source);
	}

	private static ServerSource fixed(String id, List<ServerEntry> servers) {
		return new ServerSource() {
			public String id() {
				return id;
			}

			public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
				return CompletableFuture.completedFuture(servers);
			}
		};
	}

	@Test
	void parsesMinecraftListInfo() throws IOException {
		List<ServerEntry> servers = MinecraftListInfoSource.parse(resource("minecraft-list-info.json"));
		assertEquals(3, servers.size());
		ServerEntry first = servers.get(0);
		assertFalse(first.host().isEmpty());
		assertTrue(first.port() > 0);
		assertTrue(first.iconUrl().startsWith("https://minecraft-list.info/"));
	}

	@Test
	void parsesTopMcServersCards() throws IOException {
		List<ServerEntry> servers = TopMcServersSource.parse(resource("top-mc-servers.html"), "https://top-mc-servers.net/");
		assertEquals(2, servers.size());
		ServerEntry first = servers.get(0);
		assertEquals("tm.cobblegalaxy.com", first.host());
		assertEquals("CobbleGalaxy", first.name());
		assertTrue(first.online());
		assertTrue(first.tags().contains("Cobblemon"));
		assertTrue(first.iconUrl().startsWith("https://top-mc-servers.net/"));
	}

	@Test
	void sameServerNeverShowsTwiceAndPrimaryWins() {
		ServerSource primary = fixed("primary", List.of(entry("Play.Example.com", 25565, "primary")));
		ServerSource scraped = fixed("scraped", List.of(entry("play.example.com.", 25565, "scraped"), entry("other.net", 25565, "scraped")));
		List<ServerEntry> shown = new ArrayList<>();
		new ServerSourceRegistry(List.of(primary, scraped))
				.load(Sort.POPULAR, "", null, 1, new HashSet<>(), shown::addAll).join();
		assertEquals(2, shown.size());
		assertEquals("primary", shown.get(0).source());
		assertEquals("other.net", shown.get(1).host());
	}

	@Test
	void failingSourceDoesNotStopLaterSources() {
		ServerSource broken = new ServerSource() {
			public String id() {
				return "broken";
			}

			public CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page) {
				return CompletableFuture.failedFuture(new IOException("boom"));
			}
		};
		List<ServerEntry> shown = new ArrayList<>();
		new ServerSourceRegistry(List.of(broken, fixed("ok", List.of(entry("a.net", 25565, "ok")))))
				.load(Sort.POPULAR, "", null, 1, new HashSet<>(), shown::addAll).join();
		assertEquals(1, shown.size());
	}

	@Test
	void parsesModrinthServers() throws IOException {
		List<ServerEntry> servers = ModrinthSource.parse(resource("modrinth-search.json"), resource("modrinth-projects.json"));
		assertEquals(3, servers.size());
		ServerEntry first = servers.get(0);
		assertFalse(first.host().isEmpty());
		assertEquals("modrinth", first.source());
		assertTrue(first.iconUrl().startsWith("https://cdn.modrinth.com/"));
		assertTrue(servers.stream().anyMatch(s -> !s.country().isEmpty()));
	}

	@Test
	void parsesMinecraftServersOrgWithoutPromotedRows() throws IOException {
		List<ServerEntry> servers = MinecraftServersOrgSource.parse(resource("minecraftservers-org.html"), "https://minecraftservers.org/");
		assertEquals(4, servers.size());
		assertFalse(servers.get(0).host().isEmpty());
		assertFalse(servers.get(0).name().isEmpty());
		assertTrue(servers.get(0).iconUrl().startsWith("https://"));
	}

	@Test
	void parsesMinecraftMpWithoutPremiumRows() throws IOException {
		List<ServerEntry> servers = MinecraftMpSource.parse(resource("minecraft-mp.html"), "https://minecraft-mp.com/");
		assertEquals(4, servers.size());
		assertFalse(servers.get(0).host().isEmpty());
		assertTrue(servers.stream().anyMatch(s -> s.country().length() == 2));
	}

	@Test
	void everyCategoryWithFeaturedServersHasAtLeastOne() {
		assertTrue(FeaturedSource.count(Category.BEDWARS) >= 2);
		assertTrue(FeaturedSource.count(Category.ANARCHY) >= 1);
	}

	@Test
	void regionsFilterByCountryAndKeepUnknownOnes() {
		assertTrue(com.serverify.model.Region.EUROPE.matches("PL"));
		assertFalse(com.serverify.model.Region.EUROPE.matches("US"));
		assertTrue(com.serverify.model.Region.EUROPE.matches(""));
		assertEquals("US", com.serverify.model.Region.codeOf("United_States_of_America"));
	}

	@Test
	void hostnameAliasesShareOneKey() {
		assertEquals(entry("mc.example.com", 25565, "a").key(), entry("Play.Example.com.", 25565, "b").key());
		assertEquals(entry("example.com", 25565, "a").key(), entry("www.example.com", 25565, "b").key());
		assertFalse(entry("mc.net", 25565, "a").key().equals(entry("net", 25565, "b").key()));
		assertFalse(entry("play.example.com", 25565, "a").key().equals(entry("play.example.com", 25566, "b").key()));
	}

	@Test
	void liveApiSmokeTest() {
		List<ServerEntry> servers = new MinecraftListInfoSource()
				.list(Sort.PLAYERS, "", Category.SURVIVAL, 1).orTimeout(30, java.util.concurrent.TimeUnit.SECONDS).join();
		assertFalse(servers.isEmpty());
		assertFalse(servers.get(0).host().isEmpty());
	}

	@Test
	void favoritesPersistAcrossInstances(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) {
		var file = dir.resolve("serverify").resolve("favorites.json");
		ServerEntry e = entry("fav.example.com", 25566, "x");
		assertTrue(new com.serverify.favorites.Favorites(file).toggle(e));
		var reloaded = new com.serverify.favorites.Favorites(file);
		assertTrue(reloaded.contains(e));
		assertFalse(reloaded.toggle(e));
		assertTrue(new com.serverify.favorites.Favorites(file).all().isEmpty());
	}

	@Test
	void configPersistsAndClampsRefresh(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws IOException {
		var file = dir.resolve("serverify.json");
		var config = com.serverify.config.ServerifyConfig.load(file);
		assertTrue(config.minecraftListInfoEnabled);
		config.topMcServersEnabled = false;
		config.save();
		assertFalse(com.serverify.config.ServerifyConfig.load(file).topMcServersEnabled);
		java.nio.file.Files.writeString(file, "{\"refreshSeconds\": 5}");
		assertEquals(30, com.serverify.config.ServerifyConfig.load(file).refreshSeconds);
	}
}
