// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ButtonFactory {
	private ButtonFactory() {
	}

	public static AbstractWidget create(int width, Runnable action) {
		return Button
				.builder(Component.translatable("serverify.open"), button -> action.run())
				.bounds(width - 108, 6, 100, 20)
				.build();
	}
}
