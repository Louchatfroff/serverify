// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client.gui;

import com.serverify.client.ServerifyClient;
import com.serverify.model.ServerEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Scrollable column of large server cards with hover lift, fade-in and loading placeholders. */
final class ServerListWidget extends AbstractScrollArea {
	private static final int CARD_HEIGHT = 84;
	private static final int GAP = 6;
	private static final int PAD = 8;
	private static final int ICON = 64;
	private static final int RIGHT_COLUMN = 126;
	private static final int FADE_MILLIS = 250;
	private static final int PLACEHOLDER_CARDS = 5;
	private static final int LOAD_MORE_DISTANCE = 240;
	private static final int MAX_TAGS = 4;
	private static final float HOVER_SPEED = 0.25f;

	private final Font font;
	private final List<ServerEntry> entries;
	private final Consumer<ServerEntry> onOpen;
	private final Runnable onNeedMore;
	private final Map<String, Float> hover = new HashMap<>();
	private final Map<String, Long> shownAt = new HashMap<>();
	private boolean loading;
	private int selected = -1;

	ServerListWidget(Font font, int x, int y, int width, int height, List<ServerEntry> entries,
			Consumer<ServerEntry> onOpen, Runnable onNeedMore) {
		super(x, y, width, height, Component.translatable("serverify.list"), defaultSettings((CARD_HEIGHT + GAP) / 2));
		this.font = font;
		this.entries = entries;
		this.onOpen = onOpen;
		this.onNeedMore = onNeedMore;
	}

	void setLoading(boolean loading) {
		this.loading = loading;
	}

	void reset() {
		selected = -1;
		hover.clear();
		shownAt.clear();
		setScrollAmount(0);
	}

	@Override
	protected int contentHeight() {
		int rows = entries.size() + (loading ? (entries.isEmpty() ? PLACEHOLDER_CARDS : 1) : 0);
		return rows * (CARD_HEIGHT + GAP);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		long now = Util.getMillis();
		int cardWidth = width - scrollbarWidth() - GAP;
		int top = getY() - (int) scrollAmount();
		int hoveredIndex = indexAt(mouseX, mouseY);

		g.enableScissor(getX(), getY(), getRight(), getBottom());
		for (int i = 0; i < entries.size(); i++) {
			int y = top + i * (CARD_HEIGHT + GAP);
			if (y + CARD_HEIGHT < getY() || y > getBottom()) {
				continue;
			}
			ServerEntry entry = entries.get(i);
			float target = (i == hoveredIndex || (i == selected && isFocused())) ? 1f : 0f;
			float progress = hover.merge(entry.key(), 0f, (old, ignored) -> old);
			progress += (target - progress) * HOVER_SPEED;
			hover.put(entry.key(), progress);
			drawCard(g, entry, getX(), y, cardWidth, progress, now);
		}
		if (loading) {
			int placeholders = entries.isEmpty() ? PLACEHOLDER_CARDS : 1;
			for (int i = 0; i < placeholders; i++) {
				int y = top + (entries.size() + i) * (CARD_HEIGHT + GAP);
				drawPlaceholder(g, getX(), y, cardWidth, now);
			}
		}
		g.disableScissor();

		if (scrollable()) {
			extractScrollbar(g, mouseX, mouseY);
		}
		if (entries.isEmpty() && !loading) {
			g.centeredText(font, Component.translatable("serverify.empty"), getX() + width / 2, getY() + height / 2, Ui.color(1f, Ui.MUTED));
		}
		if (!loading && !entries.isEmpty() && scrollAmount() > maxScrollAmount() - LOAD_MORE_DISTANCE) {
			onNeedMore.run();
		}
	}

	private void drawCard(GuiGraphicsExtractor g, ServerEntry e, int x, int baseY, int w, float hoverProgress, long now) {
		int y = baseY - (ServerifyClient.config.animations ? Math.round(hoverProgress * 2) : 0);
		long age = now - shownAt.computeIfAbsent(e.key(), k -> now);
		boolean animate = ServerifyClient.config.animations;
		float alpha = animate ? Mth.clamp(age / (float) FADE_MILLIS, 0f, 1f) : 1f;

		if (hoverProgress > 0.05f) {
			Ui.roundedRect(g, x - 1, y - 1, w + 2, CARD_HEIGHT + 2, Ui.color(alpha * hoverProgress, Ui.ACCENT));
		}
		Ui.roundedRect(g, x, y, w, CARD_HEIGHT, Ui.color(alpha * (0.66f + 0.2f * hoverProgress), Ui.PANEL));

		g.blit(RenderPipelines.GUI_TEXTURED, ServerifyClient.ICONS.get(e), x + PAD, y + (CARD_HEIGHT - ICON) / 2,
				0f, 0f, ICON, ICON, ICON, ICON, Ui.color(alpha, 0xFFFFFF));

		int textX = x + PAD * 2 + ICON;
		int textW = w - (textX - x) - RIGHT_COLUMN - PAD;
		int white = Ui.color(alpha, Ui.TEXT);
		int muted = Ui.color(alpha, Ui.MUTED);

		Component players = e.online()
				? Component.translatable("serverify.card.players", e.players(), e.maxPlayers())
				: Component.translatable("serverify.card.offline");
		int playersWidth = font.width(players);
		int playersX = x + w - PAD - playersWidth;
		int nameWidth = playersX - 12 - PAD - textX;
		g.text(font, Component.literal(Ui.ellipsizeBold(font, Ui.plain(e.name()), nameWidth)).withStyle(ChatFormatting.BOLD), textX, y + PAD, white);
		g.fill(playersX - 10, y + PAD + 2, playersX - 4, y + PAD + 8, Ui.color(alpha, e.online() ? Ui.ONLINE : Ui.OFFLINE));
		g.text(font, players, playersX, y + PAD, white);

		List<FormattedCharSequence> lines = font.split(FormattedText.of(Ui.plain(e.description())), textW);
		for (int i = 0; i < Math.min(2, lines.size()); i++) {
			g.text(font, lines.get(i), textX, y + PAD + 13 + i * (font.lineHeight + 1), muted);
		}
		drawTags(g, e, textX, y + CARD_HEIGHT - PAD - 10, textW, alpha);

		int rx = x + w - RIGHT_COLUMN;
		if (!e.version().isBlank()) {
			g.text(font, Ui.ellipsize(font, Ui.plain(e.version()), RIGHT_COLUMN - PAD), rx, y + PAD + 14, muted);
		}
		if (!e.country().isBlank()) {
			g.text(font, e.country(), rx, y + PAD + 26, muted);
		}
		g.text(font, Component.translatable("serverify.card.votes", e.votes()), rx, y + PAD + 38, muted);
		if (ServerifyClient.favorites != null && ServerifyClient.favorites.contains(e)) {
			g.text(font, "★", x + w - PAD - font.width("★"), y + CARD_HEIGHT - PAD - 8, Ui.color(alpha, Ui.GOLD));
		}
	}

	private void drawTags(GuiGraphicsExtractor g, ServerEntry e, int x, int y, int maxWidth, float alpha) {
		if (e.tags().isEmpty()) {
			g.text(font, Component.translatable("serverify.card.via", e.source()), x, y + 1, Ui.color(alpha * 0.7f, Ui.MUTED));
			return;
		}
		int cursor = x;
		for (String tag : e.tags().stream().limit(MAX_TAGS).toList()) {
			int pill = font.width(tag) + 8;
			if (cursor + pill > x + maxWidth) {
				break;
			}
			Ui.roundedRect(g, cursor, y, pill, 11, Ui.color(alpha * 0.8f, 0x2F5D8C));
			g.text(font, tag, cursor + 4, y + 2, Ui.color(alpha, Ui.TEXT), false);
			cursor += pill + 3;
		}
	}

	private void drawPlaceholder(GuiGraphicsExtractor g, int x, int y, int w, long now) {
		float pulse = 0.4f + 0.15f * (float) Math.sin(now / 350.0);
		Ui.roundedRect(g, x, y, w, CARD_HEIGHT, Ui.color(0.6f, Ui.PANEL));
		int block = Ui.color(pulse, 0x6A7480);
		g.fill(x + PAD, y + (CARD_HEIGHT - ICON) / 2, x + PAD + ICON, y + (CARD_HEIGHT + ICON) / 2, block);
		int textX = x + PAD * 2 + ICON;
		g.fill(textX, y + PAD, textX + w / 3, y + PAD + 9, block);
		g.fill(textX, y + PAD + 16, textX + w / 2, y + PAD + 23, Ui.color(pulse * 0.7f, 0x6A7480));
		g.fill(textX, y + PAD + 27, textX + w / 3, y + PAD + 34, Ui.color(pulse * 0.7f, 0x6A7480));
	}

	private int indexAt(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY) || mouseX >= getRight() - scrollbarWidth()) {
			return -1;
		}
		double relative = mouseY - getY() + scrollAmount();
		int index = (int) (relative / (CARD_HEIGHT + GAP));
		boolean insideCard = relative % (CARD_HEIGHT + GAP) < CARD_HEIGHT;
		return insideCard && index >= 0 && index < entries.size() ? index : -1;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (updateScrolling(event)) {
			return true;
		}
		int index = indexAt(event.x(), event.y());
		if (index >= 0 && isValidClickButton(event.buttonInfo())) {
			selected = index;
			onOpen.accept(entries.get(index));
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!isFocused() || entries.isEmpty()) {
			return false;
		}
		int key = event.key();
		if (key == InputConstants.KEY_DOWN || key == InputConstants.KEY_UP) {
			selected = Mth.clamp(selected + (key == InputConstants.KEY_DOWN ? 1 : -1), 0, entries.size() - 1);
			int cardTop = selected * (CARD_HEIGHT + GAP);
			if (cardTop < scrollAmount()) {
				setScrollAmount(cardTop);
			} else if (cardTop + CARD_HEIGHT > scrollAmount() + height) {
				setScrollAmount(cardTop + CARD_HEIGHT - height);
			}
			return true;
		}
		if ((key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) && selected >= 0) {
			onOpen.accept(entries.get(selected));
			return true;
		}
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, Component.translatable("serverify.narration.list", entries.size()));
		if (selected >= 0 && selected < entries.size()) {
			ServerEntry e = entries.get(selected);
			output.add(NarratedElementType.POSITION, Component.translatable("serverify.narration.server",
					Ui.plain(e.name()), e.players(), e.maxPlayers()));
		}
	}
}
