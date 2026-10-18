// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.compat.Lang;
import net.minecraft.client.gui.GuiButton;

public final class ButtonFactory {
	public static final int ID = 1511;

	private ButtonFactory() {
	}

	public static GuiButton create(int width) {
		return new GuiButton(ID, width - 108, 6, 100, 20, Lang.get("serverify.open"));
	}
}
