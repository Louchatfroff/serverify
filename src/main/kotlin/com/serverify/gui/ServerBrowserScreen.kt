/*
 * This file is adapted from Resourcify and is part of Serverify.
 * Copyright (C) 2023-2026 DeDiamondPro
 * Modifications copyright (C) 2026 Louchatfroff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.serverify.gui

import com.serverify.Serverify
import com.serverify.model.Category
import com.serverify.model.ServerEntry
import com.serverify.provider.ServerSource.Sort
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import net.minecraft.client.gui.screens.Screen
import java.util.Locale
import kotlin.math.min

class ServerBrowserScreen : ServerifyScreen() {
    private var query = ""
    private var category: Category? = null
    private var favoritesOnly = false
    private var onlineOnly = false
    private var sort = Sort.POPULAR
    private var page = 0
    private var generation = 0
    private var loading = false
    private var hasMore = true
    private var rawAddedThisPage = 0
    private var cardCount = 0
    private var searchDebounceTicks = 0
    private val seen = HashSet<String>()
    private val checkRows = mutableListOf<CheckRow>()

    private val contentBox = UIContainer().constrain {
        x = CenterConstraint()
        y = 4.pixels()
        width = MinConstraint(ChildBasedSizeConstraint(padding = 4f), basicWidthConstraint {
            window.getWidth() - 8
        })
        height = 100.percent()
    } childOf window

    private val sideContainer = UIContainer().constrain {
        x = 0.pixels()
        y = 0.pixels()
        width = 160.pixels()
        height = 100.percent()
    } childOf contentBox

    private val mainBox = UIContainer().constrain {
        x = 0.pixels(alignOpposite = true)
        y = 0.pixels()
        width = basicWidthConstraint { min(528f, window.getWidth() - 172) }
        height = 100.percent()
    } childOf contentBox

    private val headerBox = UIBlock(Colors.BACKGROUND).constrain {
        x = 0.pixels()
        y = SiblingConstraint()
        width = 100.percent()
        height = 29.pixels()
    } childOf mainBox

    private lateinit var searchBox: UITextInput

    private val projectScrollable = ScrollComponent(pixelsPerScroll = 30f, scrollAcceleration = 1.5f).constrain {
        x = 0.pixels()
        y = SiblingConstraint(padding = 4f)
        width = 100.percent()
        height = basicHeightConstraint { (this@ServerBrowserScreen as Screen).height - this.getY() }
    } childOf mainBox

    private val projectContainer = UIContainer().constrain {
        x = 0.pixels()
        y = SiblingConstraint(padding = 4f)
        width = 100.percent()
        height = ChildLocationSizeConstraint()
    } childOf projectScrollable

    private val statusText = UIText("").constrain {
        x = CenterConstraint()
        y = 44.pixels()
        color = Colors.TEXT_SECONDARY.toConstraint()
    } childOf mainBox

    init {
        sideBar()
        header()
        load()
    }

    private fun sideBar() {
        Paginator(this).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 160.pixels()
            height = 29.pixels()
        } childOf sideContainer

        val sideBoxScrollable = ScrollComponent(pixelsPerScroll = 30f, scrollAcceleration = 1.5f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 160.pixels()
            height = 100.percent() - 37.pixels()
        } childOf sideContainer

        val categoryContainer = UIBlock(Colors.BACKGROUND).constrain {
            x = 0.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 160.pixels()
            height = ChildLocationSizeConstraint()
        } childOf sideBoxScrollable

        UIText("serverify.browse.categories".localize()).constrain {
            x = 4.pixels()
            y = 4.pixels()
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf categoryContainer

        val categoriesBox = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 100.percent()
            height = ChildLocationSizeConstraint()
        } childOf categoryContainer

        for (entry in Category.entries) {
            addRow(categoriesBox, entry.translationKey().localize(), { category == entry }) {
                category = if (category == entry) null else entry
                reload()
            }
        }

        val showBox = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 100.percent()
            height = ChildLocationSizeConstraint()
        } childOf categoryContainer

        UIText("serverify.browse.show".localize()).constrain {
            x = 4.pixels()
            y = MaxConstraint(4.pixels(), SiblingConstraint(padding = 4f))
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf showBox
        addRow(showBox, "serverify.browse.favorites".localize(), { favoritesOnly }) {
            favoritesOnly = !favoritesOnly
            reload()
        }
        addRow(showBox, "serverify.browse.online".localize(), { onlineOnly }) {
            onlineOnly = !onlineOnly
            reload()
        }
    }

    private fun addRow(parent: UIComponent, label: String, isChecked: () -> Boolean, onToggle: () -> Unit) {
        val row = CheckRow(label, isChecked, onToggle)
        checkRows.add(row)
        row childOf parent
    }

    private fun header() {
        val searchBoxHitBox = UIContainer().constrain {
            x = 6.pixels()
            y = CenterConstraint()
            height = 19.pixels()
            width = 100.percent() - 100.pixels()
        }.onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            searchBox.grabWindowFocus()
        } childOf headerBox
        val searchUnderline = UIBlock(Colors.TEXT_SECONDARY).constrain {
            x = 0.pixels()
            y = 0.pixels(alignOpposite = true)
            width = 100.percent()
            height = 1.pixels()
        } childOf searchBoxHitBox
        searchBox = (UITextInput(
            "serverify.browse.search".localize(),
            cursorColor = Colors.TEXT_PRIMARY
        ).constrain {
            y = CenterConstraint()
            width = 100.percent()
            color = basicColorConstraint {
                if (searchBox.getText().isEmpty()) Colors.TEXT_SECONDARY else Colors.TEXT_PRIMARY
            }
        }.onUpdate {
            searchDebounceTicks = 6
        }.onFocus {
            searchUnderline.animate {
                setColorAnimation(Animations.OUT_CUBIC, 0.2f, Colors.TEXT_PRIMARY.toConstraint())
                setXAnimation(Animations.OUT_CUBIC, 0.2f, (-2).pixels())
                setWidthAnimation(Animations.OUT_CUBIC, 0.2f, 100.percent() + 4.pixels())
            }
        }.onFocusLost {
            searchUnderline.animate {
                if (searchBox.getText().isEmpty()) {
                    setColorAnimation(Animations.OUT_CUBIC, 0.2f, Colors.TEXT_SECONDARY.toConstraint())
                }
                setXAnimation(Animations.OUT_CUBIC, 0.2f, 0.pixels())
                setWidthAnimation(Animations.OUT_CUBIC, 0.2f, 100.percent())
            }
        } childOf searchBoxHitBox) as UITextInput

        val labels = Sort.entries.map { sortLabel(it) }
        DropDown(labels, onlyOneOption = true, selectedOptions = mutableListOf(sortLabel(sort))).constrain {
            x = 6.pixels(alignOpposite = true)
            y = CenterConstraint()
            width = 82.pixels()
        }.onSelectionUpdate {
            val index = labels.indexOf(it.first())
            if (index >= 0 && Sort.entries[index] != sort) {
                sort = Sort.entries[index]
                reload()
            }
        } childOf headerBox
    }

    private fun sortLabel(sort: Sort): String = "serverify.sort.${sort.name.lowercase(Locale.ROOT)}".localize()

    private fun reload() {
        checkRows.forEach { it.refresh() }
        load()
    }

    private fun load(clear: Boolean = true) {
        if (clear) {
            generation++
            seen.clear()
            page = 0
            cardCount = 0
            hasMore = true
            loading = false
            projectContainer.clearChildren()
            projectScrollable.scrollToTop(false)
        }
        if (loading || !hasMore) return
        if (favoritesOnly) {
            hasMore = false
            val needle = query.lowercase(Locale.ROOT)
            addCards(Serverify.favorites.all().filter {
                (!onlineOnly || it.online()) && (needle.isEmpty() ||
                        it.name().lowercase(Locale.ROOT).contains(needle) ||
                        it.host().lowercase(Locale.ROOT).contains(needle))
            })
            updateStatus()
            return
        }
        loading = true
        rawAddedThisPage = 0
        updateStatus()
        val token = generation
        Serverify.sources().load(sort, query, category, ++page, seen) { batch ->
            Window.enqueueRenderOperation {
                if (token != generation) return@enqueueRenderOperation
                rawAddedThisPage += batch.size
                addCards(batch.filter { !onlineOnly || it.online() })
                updateStatus()
            }
        }.whenComplete { _, _ ->
            Window.enqueueRenderOperation {
                if (token != generation) return@enqueueRenderOperation
                loading = false
                hasMore = rawAddedThisPage > 0
                updateStatus()
            }
        }
    }

    private fun addCards(servers: List<ServerEntry>) {
        for (server in servers) {
            val currentRow =
                if (projectContainer.children.isEmpty() || projectContainer.children.last().children.size >= 2) {
                    UIContainer().constrain {
                        x = 0.pixels()
                        y = SiblingConstraint(padding = 4f)
                        width = 100.percent()
                        height = ChildBasedMaxSizeConstraint()
                    } childOf projectContainer
                } else {
                    projectContainer.children.last()
                }
            ServerCard(server).constrain {
                x = SiblingConstraint(padding = 4f)
                width = 50.percent() - 2.pixels()
            } childOf currentRow
            cardCount++
        }
    }

    private fun updateStatus() {
        statusText.setText(
            when {
                cardCount > 0 -> ""
                loading -> "serverify.browse.loading".localize()
                else -> "serverify.browse.empty".localize()
            }
        )
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (modifiers?.isCtrl == true && keyCode == UKeyboard.KEY_F) {
            searchBox.grabWindowFocus()
        } else {
            super.onKeyPressed(keyCode, typedChar, modifiers)
        }
    }

    override fun onTick() {
        super.onTick()
        if (searchDebounceTicks > 0 && --searchDebounceTicks == 0) {
            query = searchBox.getText().trim()
            reload()
        } else if (projectScrollable.verticalOffset + projectScrollable.verticalOverhang < 150 && !loading && hasMore) {
            load(false)
        }
    }
}
