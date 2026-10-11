// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.model;

import java.util.Locale;

/**
 * Browser category tabs. Each source has its own slug for a category; a null slug means that
 * source has no such page, and minecraft-list.info then falls back to a name search for
 * {@link #nameTerm()}.
 */
public enum Category {
	SURVIVAL("survival", "survival"),
	SMP(null, "smp"),
	VANILLA("vanilla", "vanilla"),
	SKYBLOCK("skyblock", "skyblock"),
	PRISON("prison", "prison"),
	FACTIONS("faction", "factions"),
	PVP("pvp", "pvp"),
	KITPVP("kitpvp", "kitpvp"),
	LIFESTEAL(null, "lifesteal"),
	ONEBLOCK("oneblock", null),
	EARTH(null, null),
	TOWNY("towny", "towny"),
	ECONOMY("economy", "economy"),
	RPG("roleplay", "roleplay"),
	CREATIVE("creative", "creative"),
	MINIGAMES("minigames", "mini-games"),
	BEDWARS("bedwars", null),
	SKYWARS("skywars", null),
	PARKOUR("parkour", null),
	COBBLEMON(null, "cobblemon"),
	PIXELMON("pixelmon", "pixelmon"),
	MODDED(null, "modded"),
	HARDCORE("hardcore", null),
	ANARCHY("anarchy", "anarchy"),
	CRACKED(null, "cracked");

	private final String listInfoSlug;
	private final String topMcSlug;

	Category(String listInfoSlug, String topMcSlug) {
		this.listInfoSlug = listInfoSlug;
		this.topMcSlug = topMcSlug;
	}

	public String listInfoSlug() {
		return listInfoSlug;
	}

	public String topMcSlug() {
		return topMcSlug;
	}

	public String nameTerm() {
		return name().toLowerCase(Locale.ROOT);
	}

	/** Translation key, e.g. {@code serverify.category.skyblock}. */
	public String translationKey() {
		return "serverify.category." + nameTerm();
	}
}
