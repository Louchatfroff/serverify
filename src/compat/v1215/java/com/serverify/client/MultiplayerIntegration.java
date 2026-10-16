// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.model.ServerEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class MultiplayerIntegration {
	private MultiplayerIntegration() {
	}

	public static boolean addToList(Minecraft minecraft, ServerEntry entry) {
		ServerList list = new ServerList(minecraft);
		list.load();
		if (list.get(entry.address()) != null) {
			return false;
		}
		list.add(serverData(entry), false);
		list.save();
		return true;
	}

	public static void join(Minecraft minecraft, Screen parent, ServerEntry entry) {
		ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(entry.address()), serverData(entry), false, null);
	}

	private static ServerData serverData(ServerEntry entry) {
		String name = ChatFormatting.stripFormatting(entry.name());
		return new ServerData(name == null || name.isBlank() ? entry.host() : name.strip(), entry.address(), ServerData.Type.OTHER);
	}
}
