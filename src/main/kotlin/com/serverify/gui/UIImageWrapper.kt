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

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.image.ImageProvider
import gg.essential.elementa.renderer.ElementaExtractor
import java.awt.Color
import kotlin.math.roundToInt

object EmptyImage : ImageProvider

class UIImageWrapper(val image: UIImage) : UIComponent(), ImageProvider {
    override fun extract(
        extractor: ElementaExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color
    ) {
        image.extract(extractor, x, y, width, height, color)
    }

    override fun extractComponent(extractor: ElementaExtractor) {
        val color = this.getColor()
        if (color.alpha == 0) {
            return
        }

        val x = (this.getLeft() * extractor.guiScale).roundToInt()
        val y = (this.getTop() * extractor.guiScale).roundToInt()
        val width = (this.getWidth() * extractor.guiScale).roundToInt()
        val height = (this.getHeight() * extractor.guiScale).roundToInt()
        this.extract(extractor, x, y, width, height, color)
    }
}
