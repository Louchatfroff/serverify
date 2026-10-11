// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

/** Shared look: dark vanilla-friendly panels with small rounded corners. */
final class Ui {
	static final int PANEL = 0x101418;
	static final int ACCENT = 0x55FF55;
	static final int TEXT = 0xFFFFFF;
	static final int MUTED = 0xB0B0B0;
	static final int ONLINE = 0x55FF55;
	static final int OFFLINE = 0xFF5555;
	static final int GOLD = 0xFFAA00;

	private Ui() {
	}

	static void roundedRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
		g.fill(x + 2, y, x + w - 2, y + h, argb);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, argb);
		g.fill(x, y + 2, x + w, y + h - 2, argb);
	}

	static int color(float alpha, int rgb) {
		return ARGB.color(Math.max(0f, Math.min(1f, alpha)), rgb);
	}

	/** Server names often carry section-sign colour codes; show them as plain text. */
	static String plain(String text) {
		String stripped = ChatFormatting.stripFormatting(text);
		return stripped == null ? "" : stripped.strip();
	}

	static String ellipsize(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		return font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...";
	}

	/** Like {@link #ellipsize} but measures the text as bold, which is wider. */
	static String ellipsizeBold(Font font, String text, int maxWidth) {
		String cut = text;
		while (cut.length() > 1 && font.width(Component.literal(cut + (cut.equals(text) ? "" : "...")).withStyle(ChatFormatting.BOLD)) > maxWidth) {
			cut = cut.substring(0, cut.length() - 1);
		}
		return cut.equals(text) ? cut : cut + "...";
	}
}
