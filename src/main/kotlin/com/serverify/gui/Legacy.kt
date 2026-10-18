// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

object Legacy {
    private val FORMATTING = Regex("§.")

    fun plain(text: String): String = FORMATTING.replace(text, "").trim()
}
