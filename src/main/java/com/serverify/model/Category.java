// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.model;

import java.util.Locale;

public enum Category {
	SURVIVAL("survival", "survival", "survival-mode"),
	SMP(null, "smp", "smp"),
	VANILLA("vanilla", "vanilla", "vanilla-like"),
	SKYBLOCK("skyblock", "skyblock", "skyblock"),
	PRISON("prison", "prison", "prison"),
	FACTIONS("faction", "factions", "factions"),
	PVP("pvp", "pvp", "pvp"),
	KITPVP("kitpvp", "kitpvp", "kitpvp"),
	LIFESTEAL(null, "lifesteal", "lifesteal"),
	ONEBLOCK("oneblock", null, "oneblock"),
	EARTH(null, null, null),
	TOWNY("towny", "towny", "towns"),
	ECONOMY("economy", "economy", "economy"),
	RPG("roleplay", "roleplay", "rpg"),
	CREATIVE("creative", "creative", "creative-mode"),
	MINIGAMES("minigames", "mini-games", "minigames"),
	BEDWARS("bedwars", null, "bedwars"),
	SKYWARS("skywars", null, null),
	PARKOUR("parkour", null, "parkour"),
	COBBLEMON(null, "cobblemon", "pokemon"),
	PIXELMON("pixelmon", "pixelmon", "pokemon"),
	MODDED(null, "modded", "custom-content"),
	HARDCORE("hardcore", null, "hardcore-mode"),
	ANARCHY("anarchy", "anarchy", "anarchy"),
	CRACKED(null, "cracked", "offline-mode");

	private final String listInfoSlug;
	private final String topMcSlug;
	private final String modrinthSlug;

	Category(String listInfoSlug, String topMcSlug, String modrinthSlug) {
		this.listInfoSlug = listInfoSlug;
		this.topMcSlug = topMcSlug;
		this.modrinthSlug = modrinthSlug;
	}

	public String listInfoSlug() {
		return listInfoSlug;
	}

	public String topMcSlug() {
		return topMcSlug;
	}

	public String modrinthSlug() {
		return modrinthSlug;
	}

	public String nameTerm() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String translationKey() {
		return "serverify.category." + nameTerm();
	}
}
