// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import net.minecraft.client.gui.GuiButton;

import java.util.List;

public final class ButtonFactory {
	public static final int ID = 1511;
	private static final int ADD_SERVER_ID = 3;

	private ButtonFactory() {
	}

	public static GuiButton create(List<GuiButton> buttons, int width) {
		GuiButton add = null;
		for (GuiButton button : buttons) {
			if (button.id == ADD_SERVER_ID) {
				add = button;
			}
		}
		//#if MC>=11202
		boolean beside = add != null && add.x + add.width + 24 <= width;
		return new GuiButton(ID, beside ? add.x + add.width + 4 : width - 28, beside ? add.y : 6, 20, 20, "\u2295");
		//#else
		//$$ boolean beside = add != null && add.xPosition + add.width + 24 <= width;
		//$$ return new GuiButton(ID, beside ? add.xPosition + add.width + 4 : width - 28, beside ? add.yPosition : 6, 20, 20, "\u2295");
		//#endif
	}
}
