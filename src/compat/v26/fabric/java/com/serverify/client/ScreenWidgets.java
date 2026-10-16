// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

final class ScreenWidgets {
	private ScreenWidgets() {
	}

	static List<AbstractWidget> of(Screen screen) {
		return Screens.getWidgets(screen);
	}
}
