// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.smoke;

import com.serverify.gui.ConfigScreen;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.gui.ServerScreen;
import com.serverify.model.ServerEntry;
import gg.essential.universal.UScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ScreenShotHelper;

import java.util.Collections;
import java.util.function.Supplier;

public final class Smoke {
	private Smoke() {
	}

	public static void start() {
		Thread thread = new Thread(Smoke::run, "serverify-smoke");
		thread.setDaemon(true);
		thread.start();
	}

	private static void run() {
		Minecraft mc = null;
		try {
			while (Minecraft.getMinecraft() == null) {
				Thread.sleep(200);
			}
			mc = Minecraft.getMinecraft();
			while (!(mc.currentScreen instanceof GuiMainMenu)) {
				Thread.sleep(500);
			}
			System.out.println("serverify smoke: screen " + mc.currentScreen);
			Thread.sleep(3000);
			com.serverify.client.LivePing lp = new com.serverify.client.LivePing();
			lp.ping(new ServerEntry("2b2t.org", 25565, "2b2t", "", true, 1, 2, "1.12", "US", 0, "", Collections.singletonList("x"), "smoke")).whenComplete((r, e) -> System.out.println("serverify smoke: ping " + (r == null ? String.valueOf(e) : r.online() + " " + r.players() + "/" + r.maxPlayers() + " " + r.pingMs() + " " + r.version())));
			for (int i = 0; i < 300; i++) {
				mc.addScheduledTask(lp::tick);
				Thread.sleep(50);
			}
			show(mc, () -> new GuiMultiplayer(new GuiMainMenu()), 3000, "1-multiplayer");
			show(mc, ServerBrowserScreen::new, 15000, "2-browser");
			show(mc, () -> new ServerScreen(new ServerEntry("2b2t.org", 25565, "2b2t", "Smoke test", true, 1, 2, "1.12", "US", 0, "", Collections.singletonList("minigames"), "smoke")), 25000, "3-detail");
			show(mc, () -> new ConfigScreen(new GuiMainMenu()), 3000, "4-settings");
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			Minecraft.getMinecraft().addScheduledTask(Minecraft.getMinecraft()::shutdown);
		}
	}

	private static void show(Minecraft mc, Supplier<GuiScreen> screen, long wait, String name) throws InterruptedException {
		mc.addScheduledTask(() -> UScreen.displayScreen(screen.get()));
		Thread.sleep(wait);
		mc.addScheduledTask(() -> shot(mc, name));
		Thread.sleep(1500);
	}

	private static void shot(Minecraft mc, String name) {
		//#if MC>=11202
		ScreenShotHelper.saveScreenshot(mc.gameDir, name + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
		//#else
		//$$ ScreenShotHelper.saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
		//#endif
	}
}
