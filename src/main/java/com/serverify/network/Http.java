// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public final class Http {
	private static final int TIMEOUT_MS = 10_000;

	private Http() {
	}

	public static CompletableFuture<String> get(String url, String accept) {
		return CompletableFuture.supplyAsync(() -> new String(fetch(url, accept), StandardCharsets.UTF_8));
	}

	public static CompletableFuture<byte[]> getBytes(String url) {
		return CompletableFuture.supplyAsync(() -> fetch(url, "image/png, image/*;q=0.8"));
	}

	private static byte[] fetch(String url, String accept) {
		try {
			return attempt(url, accept);
		} catch (IOException first) {
			try {
				return attempt(url, accept);
			} catch (IOException second) {
				throw new java.util.concurrent.CompletionException(second);
			}
		}
	}

	private static byte[] attempt(String url, String accept) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		try {
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("User-Agent", "Serverify/0.1 (Minecraft client mod)");
			connection.setRequestProperty("Accept", accept);
			int status = connection.getResponseCode();
			if (status / 100 != 2) {
				throw new IOException("HTTP " + status + " for " + url);
			}
			try (InputStream in = connection.getInputStream()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				for (int read; (read = in.read(buffer)) != -1; ) {
					out.write(buffer, 0, read);
				}
				return out.toByteArray();
			}
		} finally {
			connection.disconnect();
		}
	}
}
