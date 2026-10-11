package com.serverify.client.gui;

import com.serverify.model.Category;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Sidebar of browse sections: everything, favorites, then each category. */
final class CategoryListWidget extends AbstractScrollArea {
	private static final int ROW = 18;

	/** One sidebar row. Both fields empty means "all servers". */
	record Section(Component label, Category category, boolean favorites) {
	}

	private final Font font;
	private final List<Section> sections = new ArrayList<>();
	private final Consumer<Section> onSelect;
	private int selected;

	CategoryListWidget(Font font, int x, int y, int width, int height, Section current, Consumer<Section> onSelect) {
		super(x, y, width, height, Component.translatable("serverify.categories"), defaultSettings(ROW));
		this.font = font;
		this.onSelect = onSelect;
		sections.add(new Section(Component.translatable("serverify.section.all"), null, false));
		sections.add(new Section(Component.translatable("serverify.section.favorites"), null, true));
		for (Category category : Category.values()) {
			sections.add(new Section(Component.translatable(category.translationKey()), category, false));
		}
		for (int i = 0; i < sections.size(); i++) {
			Section s = sections.get(i);
			if (s.category() == current.category() && s.favorites() == current.favorites()) {
				selected = i;
			}
		}
	}

	@Override
	protected int contentHeight() {
		return sections.size() * ROW;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
		Ui.roundedRect(g, getX(), getY(), width, height, Ui.color(0.62f, Ui.PANEL));
		g.enableScissor(getX(), getY(), getRight(), getBottom());
		int top = getY() - (int) scrollAmount();
		int hovered = indexAt(mouseX, mouseY);
		for (int i = 0; i < sections.size(); i++) {
			int y = top + i * ROW;
			if (y + ROW < getY() || y > getBottom()) {
				continue;
			}
			boolean active = i == selected;
			if (active) {
				Ui.roundedRect(g, getX() + 3, y + 1, width - scrollbarWidth() - 4, ROW - 2, Ui.color(0.35f, Ui.ACCENT));
			} else if (i == hovered) {
				Ui.roundedRect(g, getX() + 3, y + 1, width - scrollbarWidth() - 4, ROW - 2, Ui.color(0.18f, 0xFFFFFF));
			}
			g.text(font, sections.get(i).label(), getX() + 9, y + (ROW - font.lineHeight) / 2 + 1,
					Ui.color(1f, active ? Ui.TEXT : Ui.MUTED));
		}
		g.disableScissor();
		if (scrollable()) {
			extractScrollbar(g, mouseX, mouseY);
		}
	}

	private int indexAt(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY) || mouseX >= getRight() - scrollbarWidth()) {
			return -1;
		}
		int index = (int) ((mouseY - getY() + scrollAmount()) / ROW);
		return index >= 0 && index < sections.size() ? index : -1;
	}

	private void select(int index) {
		selected = index;
		onSelect.accept(sections.get(index));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (updateScrolling(event)) {
			return true;
		}
		int index = indexAt(event.x(), event.y());
		if (index >= 0 && isValidClickButton(event.buttonInfo())) {
			select(index);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!isFocused()) {
			return false;
		}
		int key = event.key();
		if (key == InputConstants.KEY_DOWN || key == InputConstants.KEY_UP) {
			select(Mth.clamp(selected + (key == InputConstants.KEY_DOWN ? 1 : -1), 0, sections.size() - 1));
			int rowTop = selected * ROW;
			if (rowTop < scrollAmount()) {
				setScrollAmount(rowTop);
			} else if (rowTop + ROW > scrollAmount() + height) {
				setScrollAmount(rowTop + ROW - height);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, Component.translatable("serverify.narration.category", sections.get(selected).label()));
	}
}
