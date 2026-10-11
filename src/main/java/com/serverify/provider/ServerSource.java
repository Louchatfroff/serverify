// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.ServerEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ServerSource {

	enum Sort { POPULAR, NEWEST, PLAYERS, NAME }

	String id();

	CompletableFuture<List<ServerEntry>> list(Sort sort, String query, Category category, int page);
}
