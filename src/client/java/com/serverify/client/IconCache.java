// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.serverify.model.ServerEntry;
import com.serverify.network.Http;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Server icons as 64x64 textures, loaded in the background on first request. Render thread only.
 * ponytail: textures live for the whole session and nothing is cached on disk; add an LRU and a
 * disk cache if memory or repeat downloads ever show up.
 */
public final class IconCache {
	private static final int SIZE = 64;
	private static final System.Logger LOG = System.getLogger("serverify");

	private final Map<String, FaviconTexture> textures = new HashMap<>();

	public Identifier get(ServerEntry entry) {
		FaviconTexture texture = textures.get(entry.key());
		if (texture == null) {
			Minecraft minecraft = Minecraft.getInstance();
			FaviconTexture created = FaviconTexture.forServer(minecraft.getTextureManager(), entry.key());
			textures.put(entry.key(), created);
			texture = created;
			fetch(entry.iconUrl())
					.exceptionallyCompose(error -> fetch(fallbackUrl(entry)))
					.thenAccept(png -> minecraft.execute(() -> upload(created, png)))
					.exceptionally(error -> {
						LOG.log(System.Logger.Level.DEBUG, "No icon for " + entry.key() + ": " + error);
						return null;
					});
		}
		return texture.textureLocation();
	}

	/** mcsrvstat.us serves a 64x64 PNG icon for any address; used when the listing's icon is missing or WebP. */
	private static String fallbackUrl(ServerEntry entry) {
		return "https://api.mcsrvstat.us/icon/" + URLEncoder.encode(entry.address(), StandardCharsets.UTF_8);
	}

	private static CompletableFuture<byte[]> fetch(String url) {
		if (url.isEmpty()) {
			return CompletableFuture.failedFuture(new IOException("no icon url"));
		}
		return Http.getBytes(url).thenApply(IconCache::toPng64);
	}

	/** Decodes any ImageIO format (not WebP) and rescales to 64x64 PNG; throws when undecodable. */
	static byte[] toPng64(byte[] raw) {
		try {
			BufferedImage source = ImageIO.read(new ByteArrayInputStream(raw));
			if (source == null) {
				throw new IOException("unsupported image format");
			}
			BufferedImage scaled = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = scaled.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(source, 0, 0, SIZE, SIZE, null);
			g.dispose();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(scaled, "png", out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}

	private static void upload(FaviconTexture texture, byte[] png) {
		if (texture.isClosed()) {
			return;
		}
		try {
			texture.upload(NativeImage.read(png));
		} catch (IOException | RuntimeException e) {
			LOG.log(System.Logger.Level.WARNING, "Could not upload icon: " + e);
		}
	}
}
