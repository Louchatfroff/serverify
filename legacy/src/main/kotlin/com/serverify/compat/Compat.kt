// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.compat

import gg.essential.universal.UResolution
import net.minecraft.client.resources.I18n

typealias Screen = net.minecraft.client.gui.GuiScreen

object Lang {
    @JvmStatic
    fun get(key: String, vararg parameters: Any): String {
        val translated = I18n.format(key, *parameters)
        if (translated != key) {
            return translated
        }
        val raw = bundled[key] ?: return key
        return try {
            String.format(raw, *parameters)
        } catch (e: Exception) {
            raw
        }
    }

    private val bundled: Map<String, String> by lazy {
        val stream = listOf("en_us.lang", "en_US.lang").firstNotNullOfOrNull { Lang::class.java.getResourceAsStream("/assets/serverify/lang/$it") }
        val result = HashMap<String, String>()
        stream?.bufferedReader(Charsets.UTF_8)?.useLines { lines ->
            lines.forEach { line ->
                val split = line.indexOf('=')
                if (split > 0) {
                    result[line.substring(0, split)] = line.substring(split + 1).replace(Regex("%(\\d+\\$)?[\\d.]*[df]"), "%$1s")
                }
            }
        }
        result
    }
}

object Scale {
    @JvmStatic
    fun of(guiScale: Int): Int {
        val limit = if (guiScale == 0) 1000 else guiScale
        var factor = 1
        while (factor < limit && UResolution.windowWidth / (factor + 1) >= 320 && UResolution.windowHeight / (factor + 1) >= 240) {
            factor++
        }
        return factor
    }
}
