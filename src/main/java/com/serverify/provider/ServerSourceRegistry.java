package com.serverify.provider;

import com.serverify.model.Category;
import com.serverify.model.ServerEntry;
import com.serverify.provider.ServerSource.Sort;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Loads sources one after another in list order (first = primary) and hands each source's
 * results to the caller as soon as they arrive. A server already in {@code seen} is dropped,
 * so the same server never shows twice and earlier sources win over later ones.
 */
public final class ServerSourceRegistry {
	private static final System.Logger LOG = System.getLogger("serverify");

	private final List<ServerSource> sources;

	public ServerSourceRegistry(List<ServerSource> sources) {
		this.sources = List.copyOf(sources);
	}

	/**
	 * @param seen owned by the caller (one per query); keep it across pages, clear it on a new query
	 * @param onBatch called on a background thread, once per source, only with unseen servers
	 */
	public CompletableFuture<Void> load(Sort sort, String query, Category category, int page, Set<String> seen,
			Consumer<List<ServerEntry>> onBatch) {
		CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
		for (ServerSource source : sources) {
			chain = chain.thenCompose(ignored -> source.list(sort, query, category, page).handle((list, error) -> {
				if (error != null) {
					LOG.log(System.Logger.Level.WARNING, "Source " + source.id() + " failed: " + error);
					return null;
				}
				List<ServerEntry> fresh = list.stream().filter(e -> seen.add(e.key())).toList();
				if (!fresh.isEmpty()) {
					onBatch.accept(fresh);
				}
				return null;
			}));
		}
		return chain;
	}
}
