// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.serverify.gui.ConfigScreen;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.gui.ServerScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

@SuppressWarnings("UnstableApiUsage")
public final class ServerifyClientGameTest implements FabricClientGameTest {
	private static final int LOAD_TICKS = 200;
	private static final int PING_TICKS = 120;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.getInput().resizeWindow(1280, 720);
		context.setScreen(() -> new JoinMultiplayerScreen(new TitleScreen()));
		context.waitForScreen(JoinMultiplayerScreen.class);
		context.waitTicks(5);
		context.takeScreenshot("1-multiplayer");

		context.clickScreenButton("serverify.open");
		context.waitForScreen(ServerBrowserScreen.class);
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("2-browser");

		click(context, 570, 300);
		context.waitForScreen(ServerScreen.class);
		context.waitTicks(PING_TICKS);
		context.takeScreenshot("3-detail");

		click(context, 560, 36);
		context.waitTicks(5);
		context.takeScreenshot("4-detail-status");

		click(context, 166, 612);
		click(context, 166, 656);
		context.waitTicks(5);
		context.takeScreenshot("5-detail-actions");

		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(ServerBrowserScreen.class);
		context.waitTicks(20);
		context.takeScreenshot("6-back");

		click(context, 1180, 36);
		context.waitTicks(10);
		context.takeScreenshot("7-dropdown");

		click(context, 1140, 106);
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("8-newest");

		click(context, 80, 124);
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("9-survival");

		click(context, 80, 124);
		context.getInput().setCursorPos(150, 400);
		context.getInput().scroll(-20);
		context.waitTicks(10);
		context.takeScreenshot("10-sidebar-bottom");

		click(context, 85, 676);
		context.waitTicks(20);
		context.takeScreenshot("11-favorites");

		click(context, 85, 676);
		context.waitTicks(LOAD_TICKS);

		click(context, 700, 36);
		context.getInput().typeChars("hypixel");
		context.waitTicks(LOAD_TICKS);
		context.takeScreenshot("12-search");

		context.setScreen(() -> new ConfigScreen(new TitleScreen()));
		context.waitForScreen(ConfigScreen.class);
		context.waitTicks(5);
		context.takeScreenshot("13-settings");

		context.setScreen(TitleScreen::new);
	}

	private static void click(ClientGameTestContext context, double x, double y) {
		context.getInput().setCursorPos(x, y);
		context.waitTicks(3);
		context.getInput().pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTicks(3);
	}
}
