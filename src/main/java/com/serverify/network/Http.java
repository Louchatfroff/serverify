// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.network;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Shared async HTTP GET with a timeout and a single retry. Never blocks the caller. */
public final class Http {
	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private Http() {
	}

	public static CompletableFuture<String> get(String url, String accept) {
		return send(url, accept, HttpResponse.BodyHandlers.ofString());
	}

	public static CompletableFuture<byte[]> getBytes(String url) {
		return send(url, "image/png, image/*;q=0.8", HttpResponse.BodyHandlers.ofByteArray());
	}

	private static <T> CompletableFuture<T> send(String url, String accept, HttpResponse.BodyHandler<T> handler) {
		return attempt(url, accept, handler).exceptionallyCompose(first -> attempt(url, accept, handler));
	}

	private static <T> CompletableFuture<T> attempt(String url, String accept, HttpResponse.BodyHandler<T> handler) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.timeout(TIMEOUT)
				.header("User-Agent", "Serverify/0.1 (Minecraft client mod)")
				.header("Accept", accept)
				.build();
		return CLIENT.sendAsync(request, handler).thenApply(response -> {
			if (response.statusCode() / 100 != 2) {
				throw new CompletionException(new IOException("HTTP " + response.statusCode() + " for " + url));
			}
			return response.body();
		});
	}
}
