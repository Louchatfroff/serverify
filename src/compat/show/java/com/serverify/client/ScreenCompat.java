// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 26.2 and newer: {@code setScreen} was renamed {@code setScreenAndShow}. */
public final class ScreenCompat {
	private ScreenCompat() {
	}

	public static void open(Minecraft minecraft, Screen screen) {
		minecraft.setScreenAndShow(screen);
	}
}
