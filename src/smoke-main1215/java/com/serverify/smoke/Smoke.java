// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.smoke;

import com.serverify.gui.ConfigScreen;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.gui.ServerScreen;
import com.serverify.model.ServerEntry;
import gg.essential.universal.UScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

import java.util.Collections;

public final class Smoke {
	private Smoke() {
	}

	public static void start() {
		Thread thread = new Thread(Smoke::run, "serverify-smoke");
		thread.setDaemon(true);
		thread.start();
	}

	private static void run() {
		try {
			while (Minecraft.getInstance() == null) {
				Thread.sleep(200);
			}
			Minecraft mc = Minecraft.getInstance();
			while (mc.getOverlay() != null || mc.screen == null) {
				Thread.sleep(500);
			}
			System.out.println("serverify smoke: screen " + mc.screen);
			Thread.sleep(3000);
			show(mc, () -> new JoinMultiplayerScreen(new TitleScreen()), 3000, "1-multiplayer");
			show(mc, ServerBrowserScreen::new, 15000, "2-browser");
			show(mc, () -> new ServerScreen(new ServerEntry("2b2t.org", 25565, "2b2t", "Smoke test", true, 1, 2, "1.21", "US", 0, "", Collections.singletonList("minigames"), "smoke")), 10000, "3-detail");
			show(mc, () -> new ConfigScreen(new TitleScreen()), 3000, "4-settings");
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());
		}
	}

	private static void show(Minecraft mc, java.util.function.Supplier<net.minecraft.client.gui.screens.Screen> screen, long wait, String name) throws InterruptedException {
		mc.execute(() -> UScreen.displayScreen(screen.get()));
		Thread.sleep(wait);
		mc.execute(() -> shot(mc, name + ".png"));
		Thread.sleep(1500);
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), 1, message -> {
		});
	}
}
