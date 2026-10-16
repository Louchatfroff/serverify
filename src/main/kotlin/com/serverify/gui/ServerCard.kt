/*
 * This file is adapted from Resourcify and is part of Serverify.
 * Copyright (C) 2023-2025 DeDiamondPro
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
import com.serverify.model.ServerEntry
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.universal.UScreen
import java.awt.Color

class ServerCard(entry: ServerEntry) : UIBlock(Colors.BACKGROUND) {
    init {
        constrain {
            x = 0.pixels()
            y = 0.pixels()
            height = CARD_HEIGHT.pixels()
        } effect ScissorEffect()

        onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            UScreen.displayScreen(ServerScreen(entry))
        }
        onMouseEnter {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.12f, Colors.BACKGROUND_HOVER.toConstraint()) }
        }
        onMouseLeave {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.12f, Colors.BACKGROUND.toConstraint()) }
        }

        val bannerHolder = UIBlock(ServerIcons.color(entry)).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 84.pixels()
        } effect ScissorEffect() childOf this

        ServerIcons.image(entry).thenAccept {
            Window.enqueueRenderOperation { bannerHolder.setColor(ServerIcons.color(entry)) }
        }

        val pillText = UIText(
            if (entry.online()) "serverify.card.players".localize(entry.players(), entry.maxPlayers())
            else "serverify.card.offline".localize()
        ).constrain {
            x = 10.pixels()
            y = CenterConstraint()
            color = Colors.TEXT_PRIMARY.toConstraint()
        }
        val pill = UIBlock(Color(0, 0, 0, 150)).constrain {
            x = 4.pixels(true)
            y = 4.pixels()
            width = basicWidthConstraint { pillText.getWidth() + 14f }
            height = 13.pixels()
        } childOf bannerHolder
        UIBlock(if (entry.online()) Colors.ONLINE else Colors.OFFLINE).constrain {
            x = 4.pixels()
            y = CenterConstraint()
            width = 4.pixels()
            height = 4.pixels()
        } childOf pill
        pillText childOf pill

        if (Serverify.favorites.contains(entry)) {
            UIText("★").constrain {
                x = 4.pixels()
                y = 4.pixels()
                color = Colors.GOLD.toConstraint()
            } childOf bannerHolder
        }

        val imageHolder = UIBlock(Colors.BACKGROUND).constrain {
            x = 4.pixels()
            y = 62.pixels()
            width = 56.pixels()
            height = 56.pixels()
        } childOf this

        ServerIcons.icon(entry).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf imageHolder

        val titleHolder = UIContainer().constrain {
            x = 64.pixels()
            y = 90.pixels()
            width = 100.percent() - 68.pixels()
            height = ChildBasedSizeConstraint(padding = 2f)
        } effect ScissorEffect() childOf this

        UIText(Legacy.plain(entry.name())).constrain {
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf titleHolder
        UIText(
            listOf(entry.country(), Legacy.plain(entry.version())).filter { it.isNotBlank() }.joinToString(" · ")
        ).constrain {
            y = SiblingConstraint(padding = 2f)
            textScale = 1.2f.pixels()
            color = Colors.TEXT_SECONDARY.toConstraint()
        } childOf titleHolder

        val description = Legacy.plain(entry.description()).let {
            if (it.length > MAX_DESCRIPTION) it.take(MAX_DESCRIPTION).trimEnd() + "..." else it
        }
        UIWrappedText(description).constrain {
            x = 4.pixels()
            y = 122.pixels()
            width = 100.percent() - 8.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf this
    }

    private companion object {
        const val CARD_HEIGHT = 168
        const val MAX_DESCRIPTION = 130
    }
}
