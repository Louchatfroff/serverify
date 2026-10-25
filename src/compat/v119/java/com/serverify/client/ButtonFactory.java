// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ButtonFactory {
	private ButtonFactory() {
	}

	public static AbstractWidget create(Screen screen, int width, Runnable action) {
		AbstractWidget add = find(screen);
		boolean beside = add != null && add.x + add.getWidth() + 24 <= width;
		return new Button(beside ? add.x + add.getWidth() + 4 : width - 28, beside ? add.y : 6, 20, 20, Component.translatable("serverify.open.icon"), button -> action.run());
	}

	private static AbstractWidget find(Screen screen) {
		String label = Component.translatable("selectServer.add").getString();
		for (Object child : screen.children()) {
			if (child instanceof AbstractWidget && ((AbstractWidget) child).getMessage().getString().equals(label)) {
				return (AbstractWidget) child;
			}
		}
		return null;
	}
}
