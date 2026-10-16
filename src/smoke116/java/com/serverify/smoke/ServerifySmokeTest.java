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

import java.util.Collections;

public final class ServerifySmokeTest implements ClientModInitializer {
	private int tick;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft mc) {
		tick++;
		if (tick == 250) {
			mc.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
		} else if (tick == 270) {
			shot(mc, "1-multiplayer");
		} else if (tick == 280) {
			mc.setScreen(new ServerBrowserScreen());
		} else if (tick == 600) {
			shot(mc, "2-browser");
		} else if (tick == 610) {
			mc.setScreen(new ServerScreen(new ServerEntry("2b2t.org", 25565, "2b2t", "Smoke test", true, 1, 2, "1.21", "US", 0, "", Collections.singletonList("minigames"), "smoke")));
		} else if (tick == 800) {
			shot(mc, "3-detail");
		} else if (tick == 810) {
			mc.setScreen(new ConfigScreen(new TitleScreen()));
		} else if (tick == 830) {
			shot(mc, "4-settings");
		} else if (tick == 850) {
			mc.stop();
		}
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name + ".png", mc.getWindow().getWidth(), mc.getWindow().getHeight(), mc.getMainRenderTarget(), message -> {
		});
	}
}
