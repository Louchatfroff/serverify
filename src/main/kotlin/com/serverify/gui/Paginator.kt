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

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UScreen

class Paginator(screen: ServerifyScreen) : UIBlock(Colors.BACKGROUND) {
    init {
        constrain {
            width = 160.pixels()
            height = 29.pixels()
        }

        val backHitBox = UIContainer().constrain {
            x = 3.pixels()
            y = CenterConstraint()
            width = 19.pixels()
            height = 19.pixels()
        }.onMouseClick { screen.goBack() } childOf this
        UIText("←").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = basicColorConstraint {
                if (ServerifyScreen.backScreens.isEmpty()) Colors.TEXT_SECONDARY else Colors.TEXT_PRIMARY
            }
        } childOf backHitBox

        val closeHitBox = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 50.pixels()
            height = 19.pixels()
        }.onMouseClick {
            UScreen.displayScreen(ServerifyScreen.backScreens.findLast { it !is ServerifyScreen })
            ServerifyScreen.cleanUp()
        } childOf this
        UIText("serverify.nav.close".localize()).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf closeHitBox

        val forwardHitBox = UIContainer().constrain {
            x = 3.pixels(true)
            y = CenterConstraint()
            width = 19.pixels()
            height = 19.pixels()
        }.onMouseClick { screen.goForward() } childOf this
        UIText("→").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = basicColorConstraint {
                if (ServerifyScreen.forwardScreens.isEmpty()) Colors.TEXT_SECONDARY else Colors.TEXT_PRIMARY
            }
        } childOf forwardHitBox
    }
}
