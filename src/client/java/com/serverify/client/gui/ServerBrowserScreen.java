// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client.gui;

import com.serverify.client.ScreenCompat;
import com.serverify.client.ServerifyClient;
import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.provider.ServerSource.Sort;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Main server browser: category sidebar, instant search, sort, and an endless list of server cards. */
public final class ServerBrowserScreen extends Screen {
	private static final int MARGIN = 8;
	private static final int SIDEBAR_WIDTH = 112;
	private static final int HEADER_HEIGHT = 40;
	private static final int FOOTER_HEIGHT = 36;
	private static final int SEARCH_DELAY_MILLIS = 300;

	private final Screen parent;
	private final List<ServerEntry> entries = new ArrayList<>();
	private final Set<String> seen = new HashSet<>();

	private Sort sort = Sort.POPULAR;
	private CategoryListWidget.Section section = new CategoryListWidget.Section(Component.empty(), null, false);
	private String query = "";
	private int page;
	private int addedThisPage;
	private int generation;
	private boolean loading;
	private boolean hasMore = true;
	private long searchDueAt;
	private double savedScroll;

	private EditBox search;
	private ServerListWidget list;
	private Button sortButton;

	public ServerBrowserScreen(Screen parent) {
		super(Component.translatable("serverify.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int contentTop = HEADER_HEIGHT;
		int contentHeight = height - HEADER_HEIGHT - FOOTER_HEIGHT;
		int listX = MARGIN + SIDEBAR_WIDTH + MARGIN;

		addRenderableWidget(new CategoryListWidget(font, MARGIN, contentTop, SIDEBAR_WIDTH, contentHeight, section, this::onSection));

		search = new EditBox(font, listX, 10, width - listX - MARGIN - 108, 20, Component.translatable("serverify.search"));
		search.setHint(Component.translatable("serverify.search.hint"));
		search.setMaxLength(64);
		search.setValue(query);
		search.setResponder(this::onQuery);
		addRenderableWidget(search);

		sortButton = addRenderableWidget(Button.builder(sortLabel(), button -> cycleSort())
				.bounds(width - MARGIN - 100, 10, 100, 20).build());

		list = addRenderableWidget(new ServerListWidget(font, listX, contentTop, width - listX - MARGIN, contentHeight,
				entries, this::open, this::loadNextPage));
		list.setScrollAmount(savedScroll);
		list.setLoading(loading);

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
				.bounds(width / 2 - 100, height - 28, 200, 20).build());

		if (entries.isEmpty() && !loading) {
			reload();
		}
	}

	private void open(ServerEntry entry) {
		savedScroll = list.scrollAmount();
		ScreenCompat.open(minecraft, new ServerDetailScreen(this, entry));
	}

	private void onSection(CategoryListWidget.Section next) {
		section = next;
		reload();
	}

	private void onQuery(String text) {
		query = text.strip();
		searchDueAt = Util.getMillis() + SEARCH_DELAY_MILLIS;
	}

	private void cycleSort() {
		Sort[] all = Sort.values();
		sort = all[(sort.ordinal() + 1) % all.length];
		sortButton.setMessage(sortLabel());
		reload();
	}

	private Component sortLabel() {
		return Component.translatable("serverify.sort", Component.translatable("serverify.sort." + sort.name().toLowerCase(Locale.ROOT)));
	}

	private void reload() {
		generation++;
		entries.clear();
		seen.clear();
		page = 0;
		hasMore = true;
		loading = false;
		savedScroll = 0;
		if (list != null) {
			list.reset();
			list.setLoading(false);
		}
		loadNextPage();
	}

	private void loadNextPage() {
		if (loading || !hasMore) {
			return;
		}
		if (section.favorites()) {
			hasMore = false;
			String needle = query.toLowerCase(Locale.ROOT);
			for (ServerEntry favorite : ServerifyClient.favorites.all()) {
				if (needle.isEmpty() || favorite.name().toLowerCase(Locale.ROOT).contains(needle)
						|| favorite.host().toLowerCase(Locale.ROOT).contains(needle)) {
					entries.add(favorite);
				}
			}
			return;
		}
		loading = true;
		addedThisPage = 0;
		list.setLoading(true);
		int token = generation;
		Category category = section.category();
		ServerifyClient.sources().load(sort, query, category, ++page, seen, batch -> minecraft.execute(() -> {
			if (token == generation) {
				entries.addAll(batch);
				addedThisPage += batch.size();
			}
		})).whenComplete((ignored, error) -> minecraft.execute(() -> {
			if (token == generation) {
				loading = false;
				hasMore = addedThisPage > 0;
				list.setLoading(false);
			}
		}));
	}

	@Override
	public void tick() {
		super.tick();
		if (searchDueAt != 0 && Util.getMillis() >= searchDueAt) {
			searchDueAt = 0;
			reload();
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.hasControlDown() && event.key() == InputConstants.KEY_F) {
			setFocused(search);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		g.text(font, title, MARGIN + 4, 16, Ui.color(1f, Ui.TEXT));
		Component status = loading
				? Component.translatable("serverify.status.loading")
				: Component.translatable("serverify.status.count", entries.size());
		g.text(font, status, MARGIN + 4, height - 22, Ui.color(1f, Ui.MUTED));
	}

	@Override
	public void onClose() {
		ScreenCompat.open(minecraft, parent);
	}
}
