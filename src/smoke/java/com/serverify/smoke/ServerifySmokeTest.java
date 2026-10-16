// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.smoke;

import com.serverify.gui.ConfigScreen;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.gui.ServerScreen;
import com.serverify.model.ServerEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

import java.util.List;

public final class ServerifySmokeTest implements ClientModInitializer {
	private int tick;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft mc) {
		tick++;
		switch (tick) {
			case 250 -> mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
			case 270 -> shot(mc, "1-multiplayer");
			case 280 -> mc.setScreen(new ServerBrowserScreen());
			case 600 -> shot(mc, "2-browser");
			case 610 -> mc.setScreen(new ServerScreen(new ServerEntry("mc.hypixel.net", 25565, "Hypixel", "Smoke test", true, 1, 2, "1.21", "US", 0, "", List.of("minigames"), "smoke")));
			case 800 -> shot(mc, "3-detail");
			case 810 -> mc.setScreen(new ConfigScreen(new TitleScreen()));
			case 830 -> shot(mc, "4-settings");
			case 850 -> mc.stop();
			default -> {
			}
		}
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), message -> {
		});
	}
}
