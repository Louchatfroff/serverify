// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional

object Legacy {
    private const val SECTION = '§'

    private val PALETTE = listOf(
        '0' to 0x000000, '1' to 0x0000AA, '2' to 0x00AA00, '3' to 0x00AAAA,
        '4' to 0xAA0000, '5' to 0xAA00AA, '6' to 0xFFAA00, '7' to 0xAAAAAA,
        '8' to 0x555555, '9' to 0x5555FF, 'a' to 0x55FF55, 'b' to 0x55FFFF,
        'c' to 0xFF5555, 'd' to 0xFF55FF, 'e' to 0xFFFF55, 'f' to 0xFFFFFF
    )

    fun plain(text: String): String = ChatFormatting.stripFormatting(text)?.trim() ?: ""

    fun format(component: Component): String {
        val out = StringBuilder()
        component.visit({ style, text ->
            if (text.isNotEmpty()) {
                out.append(SECTION).append('r')
                append(out, style)
                out.append(text)
            }
            Optional.empty<Unit>()
        }, Style.EMPTY)
        return out.toString()
    }

    private fun append(out: StringBuilder, style: Style) {
        style.color?.let { out.append(SECTION).append(nearest(it.value)) }
        if (style.isBold) out.append(SECTION).append('l')
        if (style.isItalic) out.append(SECTION).append('o')
        if (style.isUnderlined) out.append(SECTION).append('n')
        if (style.isStrikethrough) out.append(SECTION).append('m')
        if (style.isObfuscated) out.append(SECTION).append('k')
    }

    private fun nearest(rgb: Int): Char = PALETTE.minByOrNull { distance(rgb, it.second) }?.first ?: 'f'

    private fun distance(a: Int, b: Int): Int {
        val dr = ((a shr 16) and 255) - ((b shr 16) and 255)
        val dg = ((a shr 8) and 255) - ((b shr 8) and 255)
        val db = (a and 255) - (b and 255)
        return dr * dr + dg * dg + db * db
    }
}
