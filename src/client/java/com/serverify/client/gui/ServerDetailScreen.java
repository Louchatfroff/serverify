package com.serverify.client.gui;

import com.serverify.client.LivePing;
import com.serverify.client.MultiplayerIntegration;
import com.serverify.client.ScreenCompat;
import com.serverify.client.ServerifyClient;
import com.serverify.model.ServerEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Full page for one server: big icon, live status from a real ping, description and the join / add / favorite actions. */
public final class ServerDetailScreen extends Screen {
	private static final int MARGIN = 16;
	private static final int ICON = 96;
	private static final int ICON_TEXTURE = 64;
	private static final int BUTTON_WIDTH = 120;
	private static final int BUTTON_GAP = 6;
	private static final int TICKS_PER_SECOND = 20;
	private static final int GOOD_PING_MS = 150;
	private static final int OK_PING_MS = 300;
	private static final int MOTD_LINES = 2;

	private final Screen parent;
	private final LivePing livePing = new LivePing();
	private ServerEntry entry;
	private LivePing.Result live;
	private boolean checking;
	private boolean statusFailed;
	private int ticks;
	private boolean refreshedOnOpen;

	public ServerDetailScreen(Screen parent, ServerEntry entry) {
		super(Component.literal(Ui.plain(entry.name())));
		this.parent = parent;
		this.entry = entry;
	}

	@Override
	protected void init() {
		int rowWidth = 3 * BUTTON_WIDTH + 2 * BUTTON_GAP;
		int left = (width - rowWidth) / 2;
		int topRow = height - 52;
		int bottomRow = height - 28;

		addRenderableWidget(Button.builder(Component.translatable("serverify.detail.join"),
				button -> MultiplayerIntegration.join(minecraft, this, entry)).bounds(left, topRow, BUTTON_WIDTH, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("serverify.detail.add"), button -> {
			boolean added = MultiplayerIntegration.addToList(minecraft, entry);
			button.setMessage(Component.translatable(added ? "serverify.detail.added" : "serverify.detail.already"));
			button.active = false;
		}).bounds(left + BUTTON_WIDTH + BUTTON_GAP, topRow, BUTTON_WIDTH, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("serverify.detail.copy"), button -> {
			minecraft.keyboardHandler.setClipboard(entry.address());
			button.setMessage(Component.translatable("serverify.detail.copied"));
		}).bounds(left + 2 * (BUTTON_WIDTH + BUTTON_GAP), topRow, BUTTON_WIDTH, 20).build());

		addRenderableWidget(Button.builder(favoriteLabel(), button -> {
			ServerifyClient.favorites.toggle(entry);
			button.setMessage(favoriteLabel());
		}).bounds(left, bottomRow, BUTTON_WIDTH, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("serverify.detail.refresh"), button -> refreshStatus())
				.bounds(left + BUTTON_WIDTH + BUTTON_GAP, bottomRow, BUTTON_WIDTH, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
				.bounds(left + 2 * (BUTTON_WIDTH + BUTTON_GAP), bottomRow, BUTTON_WIDTH, 20).build());

		if (!refreshedOnOpen) {
			refreshedOnOpen = true;
			refreshStatus();
		}
	}

	private Component favoriteLabel() {
		return Component.translatable(ServerifyClient.favorites.contains(entry) ? "serverify.detail.unfavorite" : "serverify.detail.favorite");
	}

	private void refreshStatus() {
		if (checking) {
			return;
		}
		checking = true;
		statusFailed = false;
		livePing.ping(entry).whenComplete((result, error) -> minecraft.execute(() -> {
			checking = false;
			if (error != null) {
				statusFailed = true;
			} else {
				live = result;
				entry = entry.withStatus(result.online(), result.players(), result.maxPlayers(),
						result.online() ? result.version() : entry.version());
			}
		}));
	}

	@Override
	public void tick() {
		super.tick();
		livePing.tick();
		if (++ticks % (ServerifyClient.config.refreshSeconds * TICKS_PER_SECOND) == 0) {
			refreshStatus();
		}
	}

	@Override
	public void removed() {
		livePing.close();
	}

	private static int pingColor(long ms) {
		return ms < GOOD_PING_MS ? Ui.ONLINE : ms < OK_PING_MS ? Ui.GOLD : Ui.OFFLINE;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		super.extractRenderState(g, mouseX, mouseY, a);
		int panelX = MARGIN;
		int panelY = 12;
		int panelW = width - 2 * MARGIN;
		int panelH = height - 60 - panelY;
		Ui.roundedRect(g, panelX, panelY, panelW, panelH, Ui.color(0.72f, Ui.PANEL));

		int iconX = panelX + 12;
		int iconY = panelY + 12;
		g.blit(RenderPipelines.GUI_TEXTURED, ServerifyClient.ICONS.get(entry), iconX, iconY, 0f, 0f, ICON, ICON,
				ICON_TEXTURE, ICON_TEXTURE, ICON_TEXTURE, ICON_TEXTURE, 0xFFFFFFFF);

		int textX = iconX + ICON + 14;
		int textW = panelX + panelW - 12 - textX;
		float titleScale = 1.5f;
		g.pose().pushMatrix();
		g.pose().translate(textX, iconY);
		g.pose().scale(titleScale, titleScale);
		g.text(font, Component.literal(Ui.ellipsizeBold(font, Ui.plain(entry.name()), (int) (textW / titleScale)))
				.withStyle(ChatFormatting.BOLD), 0, 0, 0xFFFFFFFF);
		g.pose().popMatrix();

		int line = iconY + 20;
		int step = font.lineHeight + 4;
		g.text(font, Component.translatable("serverify.detail.address", entry.address()), textX, line, Ui.color(1f, Ui.TEXT));
		line += step;
		g.fill(textX, line + 1, textX + 6, line + 7, Ui.color(1f, checking && live == null ? Ui.GOLD : entry.online() ? Ui.ONLINE : Ui.OFFLINE));
		Component status = checking && live == null ? Component.translatable("serverify.status.checking")
				: statusFailed && live == null ? Component.translatable("serverify.status.failed")
				: entry.online() ? Component.translatable("serverify.detail.online", entry.players(), entry.maxPlayers())
				: Component.translatable("serverify.card.offline");
		g.text(font, status, textX + 10, line, Ui.color(1f, Ui.TEXT));
		if (live != null && live.online()) {
			g.text(font, Component.translatable("serverify.detail.ping", live.pingMs()),
					textX + 10 + font.width(status) + 10, line, Ui.color(1f, pingColor(live.pingMs())));
		}
		line += step;
		if (!entry.version().isBlank()) {
			g.text(font, Component.translatable("serverify.detail.version", Ui.ellipsize(font, Ui.plain(entry.version()), textW - 60)),
					textX, line, Ui.color(1f, Ui.MUTED));
			line += step;
		}
		if (!entry.country().isBlank()) {
			g.text(font, Component.translatable("serverify.detail.country", entry.country()), textX, line, Ui.color(1f, Ui.MUTED));
			line += step;
		}
		g.text(font, Component.translatable("serverify.card.votes", entry.votes()), textX, line, Ui.color(1f, Ui.MUTED));
		line += step;
		g.text(font, Component.translatable("serverify.card.via", entry.source()), textX, line, Ui.color(0.7f, Ui.MUTED));

		int cursorY = iconY + ICON + 14;
		cursorY = drawTags(g, iconX, cursorY, panelX + panelW - 12);
		cursorY = drawMotd(g, iconX, cursorY, panelW - 24);
		List<FormattedCharSequence> lines = font.split(FormattedText.of(Ui.plain(entry.description())), panelW - 24);
		int maxLines = Math.max(0, (panelY + panelH - 8 - cursorY) / (font.lineHeight + 2));
		for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
			g.text(font, lines.get(i), iconX, cursorY + i * (font.lineHeight + 2), Ui.color(1f, Ui.MUTED));
		}
	}

	private int drawTags(GuiGraphicsExtractor g, int x, int y, int right) {
		if (entry.tags().isEmpty()) {
			return y;
		}
		int cursor = x;
		for (String tag : entry.tags()) {
			int pill = font.width(tag) + 8;
			if (cursor + pill > right) {
				break;
			}
			Ui.roundedRect(g, cursor, y, pill, 11, Ui.color(0.8f, 0x2F5D8C));
			g.text(font, tag, cursor + 4, y + 2, 0xFFFFFFFF, false);
			cursor += pill + 3;
		}
		return y + 18;
	}

	private int drawMotd(GuiGraphicsExtractor g, int x, int y, int width) {
		if (live == null || !live.online()) {
			return y;
		}
		List<FormattedCharSequence> motd = font.split(live.motd(), width - 8);
		int shown = Math.min(MOTD_LINES, motd.size());
		if (shown == 0) {
			return y;
		}
		int boxHeight = shown * (font.lineHeight + 1) + 6;
		Ui.roundedRect(g, x, y, width, boxHeight, Ui.color(0.8f, 0x000000));
		for (int i = 0; i < shown; i++) {
			g.text(font, motd.get(i), x + 4, y + 3 + i * (font.lineHeight + 1), 0xFFFFFFFF);
		}
		return y + boxHeight + 6;
	}

	@Override
	public void onClose() {
		ScreenCompat.open(minecraft, parent);
	}
}
