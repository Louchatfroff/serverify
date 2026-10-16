// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

public final class ButtonFactory {
	private ButtonFactory() {
	}

	public static AbstractWidget create(int width, Runnable action) {
		return new Button(width - 108, 6, 100, 20, new TranslatableComponent("serverify.open"), button -> action.run());
	}
}
