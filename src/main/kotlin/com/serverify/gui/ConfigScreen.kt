// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

import com.serverify.Serverify
import com.serverify.config.ServerifyConfig
import com.serverify.client.RenderMode
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import gg.essential.universal.UScreen
import com.serverify.compat.Screen
import java.awt.Color

class ConfigScreen(private val parent: Screen?) : WindowScreen(
    version = ElementaVersion.V11,
    drawDefaultBackground = true
) {
    private val panel = UIBlock(Colors.BACKGROUND).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 220.pixels()
        height = 182.pixels()
    } childOf window

    init {
        useElementaRenderer = RenderMode.EXTRACT
        val config = Serverify.config

        UIText("serverify.config.title".localize()).constrain {
            x = CenterConstraint()
            y = 8.pixels()
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf panel

        toggle("serverify.config.source.modrinth", { config.modrinthEnabled }) {
            config.modrinthEnabled = !config.modrinthEnabled
        }
        toggle("serverify.config.source.listinfo", { config.minecraftListInfoEnabled }) {
            config.minecraftListInfoEnabled = !config.minecraftListInfoEnabled
        }
        toggle("serverify.config.source.topmc", { config.topMcServersEnabled }) {
            config.topMcServersEnabled = !config.topMcServersEnabled
        }
        toggle("serverify.config.source.more", { config.moreSourcesEnabled }) {
            config.moreSourcesEnabled = !config.moreSourcesEnabled
        }
        button({ "serverify.config.refresh".localize(config.refreshSeconds) }) {
            val next = config.refreshSeconds + REFRESH_STEP_SECONDS
            config.refreshSeconds =
                if (next > ServerifyConfig.MAX_REFRESH_SECONDS) ServerifyConfig.MIN_REFRESH_SECONDS else next
        }
        button({ "serverify.config.done".localize() }, gap = 14f, save = false) { close() }
    }

    private fun toggle(key: String, value: () -> Boolean, flip: () -> Unit) {
        button({ key.localize((if (value()) "options.on" else "options.off").localize()) }, action = flip)
    }

    private fun button(label: () -> String, gap: Float = 6f, save: Boolean = true, action: () -> Unit) {
        val text = UIText(label()).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = Colors.TEXT_PRIMARY.toConstraint()
        }
        val block = UIBlock(Colors.BUTTON_SECONDARY).constrain {
            x = 8.pixels()
            y = SiblingConstraint(padding = gap)
            width = 100.percent() - 16.pixels()
            height = 18.pixels()
        } childOf panel
        block.onMouseEnter {
            block.animate { setColorAnimation(Animations.OUT_QUAD, 0.1f, HOVER.toConstraint()) }
        }
        block.onMouseLeave {
            block.animate { setColorAnimation(Animations.OUT_QUAD, 0.1f, Colors.BUTTON_SECONDARY.toConstraint()) }
        }
        block.onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            action()
            text.setText(label())
            if (save) Serverify.config.save()
        }
        text childOf block
    }

    private fun close() {
        UScreen.displayScreen(parent)
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (keyCode == UKeyboard.KEY_ESCAPE) close() else super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    private companion object {
        const val REFRESH_STEP_SECONDS = 15
        val HOVER = Color(0x9a, 0x9c, 0xaa, 0xff)
    }
}
