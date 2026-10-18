// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

import com.serverify.compat.Lang

fun String.localize(vararg parameters: Any): String = Lang.get(this, *parameters)
