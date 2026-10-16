// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.network;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Async {
	private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "serverify-timeout");
		thread.setDaemon(true);
		return thread;
	});

	private Async() {
	}

	public static <T> CompletableFuture<T> timeout(CompletableFuture<T> future, long seconds) {
		TIMER.schedule(() -> future.completeExceptionally(new TimeoutException()), seconds, TimeUnit.SECONDS);
		return future;
	}
}
