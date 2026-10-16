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

import com.serverify.client.RenderMode
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMinecraft
import gg.essential.universal.UResolution
import net.minecraft.client.gui.screens.Screen
import kotlin.math.floor

abstract class ServerifyScreen(
    private val adaptScale: Boolean = true,
    private val minimumWidth: Float = 500f
) : WindowScreen(
    version = ElementaVersion.V11,
    drawDefaultBackground = true
) {
    private var defaultScale = -1

    init {
        useElementaRenderer = RenderMode.EXTRACT

        if (!replacingScreen) {
            currentScreen?.let { backScreens.add(it) }
            forwardScreens.clear()
        }
    }

    override fun onTick() {
        if (!adaptScale) {
            super.onTick()
            return
        }
        if (defaultScale == -1) defaultScale = UMinecraft.guiScale
        val updatedScale = getGuiScale(defaultScale, minimumWidth)
        if (updatedScale != UResolution.scaleFactor.toInt()) {
            newGuiScale = updatedScale
            updateGuiScale()
            UMinecraft.guiScale = updatedScale
            (this as Screen).width = UResolution.scaledWidth
            (this as Screen).height = UResolution.scaledHeight
        }
        super.onTick()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (window.focusedComponent == null && keyCode == UKeyboard.KEY_ESCAPE) {
            goBack()
        } else {
            super.onKeyPressed(keyCode, typedChar, modifiers)
        }
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        when (mouseButton) {
            3 -> goBack()
            4 -> goForward()
            else -> super.onMouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    open fun goBack() {
        val backScreen = backScreens.removeLastOrNull()
        if (backScreen is ServerifyScreen) forwardScreens.add(this)
        else cleanUp()
        displayScreen(backScreen)
    }

    open fun goForward() {
        val forwardScreen = forwardScreens.removeLastOrNull() ?: return
        backScreens.add(this)
        displayScreen(forwardScreen)
    }

    companion object {
        private var replacingScreen = false
        val backScreens: MutableList<Screen> = mutableListOf()
        val forwardScreens: MutableList<Screen> = mutableListOf()

        fun cleanUp() {
            backScreens.clear()
            forwardScreens.clear()
            ServerIcons.clear()
        }

        private fun getGuiScale(defaultScale: Int, target: Float): Int {
            val minScale = floor(UResolution.windowWidth / target).toInt()
            return minScale.coerceAtLeast(1).coerceAtMost(calculateScaleFactor(defaultScale))
        }

        private fun calculateScaleFactor(guiScale: Int): Int {
            val mc = UMinecraft.getMinecraft()
            return mc.window.calculateScale(guiScale, mc.isEnforceUnicode)
        }
    }
}
