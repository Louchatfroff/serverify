// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

import net.minecraft.client.resources.language.I18n

fun String.localize(vararg parameters: Any): String = I18n.get(this, *parameters)
