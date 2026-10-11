// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gametest;

import com.serverify.client.gui.ConfigScreen;
import com.serverify.client.gui.ServerBrowserScreen;
import com.serverify.client.gui.ServerDetailScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import com.mojang.blaze3d.platform.InputConstants;

/** Drives the real client through the browser and saves screenshots under build/run/clientGameTest/screenshots. */
@SuppressWarnings("UnstableApiUsage")
public final class ServerifyClientGameTest implements FabricClientGameTest {
	private static final int LOAD_TICKS = 160;
	private static final int PING_TICKS = 100;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.setScreen(() -> new JoinMultiplayerScreen(new TitleScreen()));
		context.waitForScreen(JoinMultiplayerScreen.class);
		context.waitTicks(5);
		context.takeScreenshot("1-multiplayer-with-button");

		context.clickScreenButton("serverify.open");
		context.waitForScreen(ServerBrowserScreen.class);
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("2-browser-loaded");

		context.getInput().resizeWindow(1280, 720);
		context.waitTicks(10);
		context.takeScreenshot("3-browser-large-window");
		context.getInput().resizeWindow(854, 480);
		context.waitTicks(10);

		double scale = context.computeOnClient(mc -> mc.getWindow().getGuiScale());
		click(context, scale, 300, 100);
		context.waitForScreen(ServerDetailScreen.class);
		context.waitTicks(PING_TICKS);
		context.takeScreenshot("4-detail-first-server");

		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(ServerBrowserScreen.class);
		context.waitTicks(10);
		context.takeScreenshot("5-back-in-browser");

		click(context, scale, 50, 85);
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("6-category-survival");

		click(context, scale, 50, 49);
		context.waitTicks(LOAD_TICKS);
		click(context, scale, 300, 20);
		context.getInput().typeChars("hypixel");
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("7-search");

		click(context, scale, 300, 100);
		context.waitForScreen(ServerDetailScreen.class);
		context.waitTicks(PING_TICKS);
		context.takeScreenshot("8-detail-hypixel");

		context.clickScreenButton("serverify.detail.add");
		context.waitTicks(5);
		context.clickScreenButton("serverify.detail.favorite");
		context.waitTicks(5);
		context.takeScreenshot("9-favorited");

		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(ServerBrowserScreen.class);
		click(context, scale, 50, 67);
		context.waitTicks(20);
		context.takeScreenshot("10-favorites-view");

		context.setScreen(() -> new ConfigScreen(new TitleScreen()));
		context.waitForScreen(ConfigScreen.class);
		click(context, scale, 213, 110);
		context.waitTicks(5);
		context.takeScreenshot("11-config");

		context.setScreen(TitleScreen::new);
	}

	private static void click(ClientGameTestContext context, double scale, double guiX, double guiY) {
		context.getInput().setCursorPos(guiX * scale, guiY * scale);
		context.waitTicks(2);
		context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTicks(2);
	}
}
