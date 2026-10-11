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
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

class CheckRow(
    label: String,
    private val isChecked: () -> Boolean,
    private val onToggle: () -> Unit
) : UIContainer() {
    private val enabledColor = Colors.CHECKBOX
    private val disabledColor = Color(enabledColor.red, enabledColor.green, enabledColor.blue, 0)

    private val check = UIBlock(if (isChecked()) enabledColor else disabledColor).constrain {
        x = 1.pixels()
        y = 1.pixels()
        width = 5.pixels()
        height = 5.pixels()
    }

    init {
        constrain {
            x = 4.pixels()
            y = SiblingConstraint(4f)
            width = ChildBasedSizeConstraint(4f)
            height = ChildBasedMaxSizeConstraint()
        }
        val checkBox = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 7.pixels()
            height = 7.pixels()
        } effect OutlineEffect(Colors.CHECKBOX, 1f) childOf this
        check childOf checkBox
        UIText(label).constrain {
            x = SiblingConstraint(padding = 4f)
            y = 0.pixels()
            color = Colors.TEXT_SECONDARY.toConstraint()
        } childOf this
        onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            onToggle()
        }
    }

    fun refresh() {
        check.animate {
            setColorAnimation(
                Animations.IN_OUT_QUAD,
                0.15f,
                (if (isChecked()) enabledColor else disabledColor).toConstraint(),
                0f
            )
        }
    }
}
