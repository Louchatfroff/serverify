// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client.gui;

import com.serverify.client.ScreenCompat;
import com.serverify.client.ServerifyClient;
import com.serverify.config.ServerifyConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Settings page opened from Mod Menu. Every change is saved immediately. */
public final class ConfigScreen extends Screen {
	private static final int ROW_WIDTH = 220;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_STEP = 26;
	private static final int REFRESH_STEP_SECONDS = 15;

	private final Screen parent;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("serverify.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ServerifyConfig config = ServerifyClient.config;
		int x = (width - ROW_WIDTH) / 2;
		int y = 48;

		addRenderableWidget(toggle(x, y, "serverify.config.source.listinfo", config.minecraftListInfoEnabled, value -> config.minecraftListInfoEnabled = value));
		addRenderableWidget(toggle(x, y + ROW_STEP, "serverify.config.source.topmc", config.topMcServersEnabled, value -> config.topMcServersEnabled = value));
		addRenderableWidget(toggle(x, y + 2 * ROW_STEP, "serverify.config.animations", config.animations, value -> config.animations = value));
		addRenderableWidget(Button.builder(refreshLabel(config), button -> {
			int next = config.refreshSeconds + REFRESH_STEP_SECONDS;
			config.refreshSeconds = next > ServerifyConfig.MAX_REFRESH_SECONDS ? ServerifyConfig.MIN_REFRESH_SECONDS : next;
			config.save();
			button.setMessage(refreshLabel(config));
		}).bounds(x, y + 3 * ROW_STEP, ROW_WIDTH, ROW_HEIGHT).build());

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(x, height - 28, ROW_WIDTH, ROW_HEIGHT).build());
	}

	private static Component refreshLabel(ServerifyConfig config) {
		return Component.translatable("serverify.config.refresh", config.refreshSeconds);
	}

	private static Button toggle(int x, int y, String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
		boolean[] state = {initial};
		return Button.builder(toggleLabel(key, initial), button -> {
			state[0] = !state[0];
			setter.accept(state[0]);
			ServerifyClient.config.save();
			button.setMessage(toggleLabel(key, state[0]));
		}).bounds(x, y, ROW_WIDTH, ROW_HEIGHT).build();
	}

	private static Component toggleLabel(String key, boolean value) {
		return Component.translatable(key, value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		g.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ScreenCompat.open(minecraft, parent);
	}
}
