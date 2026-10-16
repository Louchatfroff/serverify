// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.smoke;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.serverify.gui.ConfigScreen;
import com.serverify.gui.ServerBrowserScreen;
import com.serverify.gui.ServerScreen;
import com.serverify.model.ServerEntry;
import gg.essential.universal.UScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.function.Consumer;

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
			for (int waited = 0; !(UScreen.getCurrentScreen() instanceof TitleScreen) && waited < 240; waited++) {
				Thread.sleep(500);
			}
			System.out.println("serverify smoke: screen " + UScreen.getCurrentScreen());
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
		try {
			Consumer<Object> ignored = message -> {
			};
			java.util.List<Method> overloads = new java.util.ArrayList<>(java.util.Arrays.asList(Screenshot.class.getMethods()));
			overloads.sort((a, b) -> b.getParameterCount() - a.getParameterCount());
			for (Method method : overloads) {
				if (!method.getName().equals("grab") || !Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				Class<?>[] types = method.getParameterTypes();
				boolean target = false;
				int ints = 0;
				for (Class<?> type : types) {
					target |= type == RenderTarget.class;
					ints += type == int.class ? 1 : 0;
				}
				if (!target) {
					continue;
				}
				Object[] args = new Object[types.length];
				int seenInts = 0;
				for (int i = 0; i < types.length; i++) {
					Class<?> type = types[i];
					if (type == File.class) {
						args[i] = mc.gameDirectory;
					} else if (type == String.class) {
						args[i] = name;
					} else if (type == RenderTarget.class) {
						args[i] = mc.getMainRenderTarget();
					} else if (type == int.class) {
						int value = ints == 2 ? (seenInts == 0 ? mc.getWindow().getWidth() : mc.getWindow().getHeight()) : 1;
						args[i] = value;
						seenInts++;
					} else if (type == Consumer.class) {
						args[i] = ignored;
					}
				}
				method.invoke(null, args);
				return;
			}
			System.err.println("serverify smoke: no usable Screenshot.grab overload");
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}
}
