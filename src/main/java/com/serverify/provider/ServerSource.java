// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.ServerEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** A place servers come from. Featured/popular/newest are just {@link Sort} presets. */
public interface ServerSource {

	enum Sort { POPULAR, NEWEST, PLAYERS, NAME }

	/** Short id shown in logs and stored on each {@link ServerEntry}. */
	String id();

	/**
	 * One page of servers (1-based). {@code query} may be blank and {@code category} null. Sources
	 * that cannot honour a sort, query or category complete with an empty list instead of
	 * returning wrong results.
	 */
	CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page);
}
