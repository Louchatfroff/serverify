// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.serverify.model.ServerEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraftforge.fml.client.FMLClientHandler;

public final class MultiplayerIntegration {
	private MultiplayerIntegration() {
	}

	public static boolean addToList(Minecraft minecraft, ServerEntry entry) {
		ServerList list = new ServerList(minecraft);
		list.loadServerList();
		for (int i = 0; i < list.countServers(); i++) {
			if (list.getServerData(i).serverIP.equalsIgnoreCase(entry.address())) {
				return false;
			}
		}
		list.addServerData(serverData(entry));
		list.saveServerList();
		return true;
	}

	public static void join(Minecraft minecraft, GuiScreen parent, ServerEntry entry) {
		FMLClientHandler.instance().connectToServer(parent, serverData(entry));
	}

	private static ServerData serverData(ServerEntry entry) {
		String name = entry.name().replaceAll("§.", "").trim();
		return new ServerData(name.isEmpty() ? entry.host() : name, entry.address(), false);
	}
}
