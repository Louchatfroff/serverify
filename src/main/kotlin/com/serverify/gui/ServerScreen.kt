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

import com.serverify.client.LivePing
import com.serverify.client.PingResult
import com.serverify.client.MultiplayerIntegration
import com.serverify.Serverify
import com.serverify.model.ServerEntry
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.ChatColor
import gg.essential.universal.UDesktop
import gg.essential.universal.UMinecraft
import com.serverify.compat.Screen
import java.awt.Color
import kotlin.math.min

class ServerScreen(private var entry: ServerEntry) : ServerifyScreen() {
    private val livePing = LivePing()
    private var live: PingResult? = null
    private var checking = false
    private var failed = false
    private var tickCounter = 0
    private val updaters = mutableListOf<() -> Unit>()

    private val scrollBox = ScrollComponent(pixelsPerScroll = 30f, scrollAcceleration = 1.5f).constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf window

    private val contentBox = UIContainer().constrain {
        x = CenterConstraint()
        y = 4.pixels()
        width = MinConstraint(ChildBasedSizeConstraint(padding = 4f), basicWidthConstraint {
            window.getWidth() - 8
        })
        height = ChildLocationSizeConstraint() + 4.pixels()
    } childOf scrollBox

    private val sideContainer = UIContainer().constrain {
        x = 0.pixels()
        y = 0.pixels()
        width = 160.pixels()
        height = ChildBasedSizeConstraint()
    } childOf contentBox

    private val mainBox = UIContainer().constrain {
        x = 0.pixels(alignOpposite = true)
        y = 0.pixels()
        width = basicWidthConstraint { min(528f, window.getWidth() - 172) }
        height = ChildLocationSizeConstraint()
    } childOf contentBox

    init {
        mainBody()
        sideBar()
        refreshStatus()
    }

    private fun statusLine(): String = when {
        checking && live == null -> "serverify.status.checking".localize()
        failed && live == null -> "serverify.status.failed".localize()
        entry.online() -> "serverify.detail.online".localize(entry.players(), entry.maxPlayers())
        else -> "serverify.card.offline".localize()
    }

    private fun pingLine(): String = live?.takeIf { it.online() }?.let { "serverify.detail.ping".localize(it.pingMs()) } ?: ""

    private fun pingColor(): Color {
        val ms = live?.pingMs() ?: return Colors.TEXT_SECONDARY
        return when {
            ms < GOOD_PING_MS -> Colors.ONLINE
            ms < OK_PING_MS -> Colors.GOLD
            else -> Colors.OFFLINE
        }
    }

    private fun mainBody() {
        val navigationBox = UIBlock(Colors.BACKGROUND).constrain {
            x = 0.pixels(alignOpposite = true)
            y = SiblingConstraint()
            width = 100.percent()
            height = 29.pixels()
        } childOf mainBox

        val joinText = UIText("${ChatColor.BOLD}${"serverify.detail.join".localize()}").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = Colors.TEXT_PRIMARY.toConstraint()
        }
        val joinButton = UIBlock(Colors.BUTTON_PRIMARY).constrain {
            x = 6.pixels(true)
            y = CenterConstraint()
            width = basicWidthConstraint { joinText.getWidth() + 8f }
            height = 18.pixels()
        }.onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            MultiplayerIntegration.join(UMinecraft.getMinecraft(), this@ServerScreen as Screen, entry)
        } childOf navigationBox
        joinText childOf joinButton

        val descriptionPage = descriptionPage() childOf mainBox
        val statusPage = statusPage() childOf mainBox
        statusPage.hide(instantly = true)
        var current: UIComponent = descriptionPage
        val pages = linkedMapOf(
            "serverify.detail.tab.description".localize() to descriptionPage,
            "serverify.detail.tab.status".localize() to statusPage
        )
        pages.forEach { (text, page) ->
            val hitBox = UIContainer().constrain {
                x = if (page === descriptionPage) 6.pixels() else SiblingConstraint(padding = 8f)
                y = CenterConstraint()
                width = ChildBasedSizeConstraint()
                height = 19.pixels()
            }.onMouseClick {
                if (page === current || it.mouseButton != 0) return@onMouseClick
                current.hide()
                page.unhide()
                current = page
            } childOf navigationBox

            UIText("${ChatColor.BOLD}$text").constrain {
                y = CenterConstraint()
                color = Colors.TEXT_PRIMARY.toConstraint()
            } childOf hitBox
        }
    }

    private fun page(): UIBlock = UIBlock(Colors.BACKGROUND).constrain {
        x = 0.pixels()
        y = SiblingConstraint(padding = 4f)
        width = 100.percent()
        height = ChildLocationSizeConstraint() + 8.pixels()
    }

    private fun descriptionPage(): UIComponent {
        val page = page()
        UIWrappedText(Legacy.plain(entry.name())).constrain {
            x = 6.pixels()
            y = 6.pixels()
            width = 100.percent() - 12.pixels()
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf page

        val motdBox = UIBlock(Color(0, 0, 0, 160)).constrain {
            x = 6.pixels()
            y = SiblingConstraint(padding = 6f)
            width = 100.percent() - 12.pixels()
            height = ChildLocationSizeConstraint() + 6.pixels()
        } childOf page
        val motdText = UIWrappedText("").constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf motdBox
        motdBox.hide(instantly = true)
        updaters.add {
            val motd = live?.takeIf { it.online() }?.motd()
            if (motd == null) motdBox.hide()
            else {
                motdText.setText(motd)
                motdBox.unhide()
            }
        }

        UIWrappedText(Legacy.plain(entry.description())).constrain {
            x = 6.pixels()
            y = SiblingConstraint(padding = 8f)
            width = 100.percent() - 12.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf page
        return page
    }

    private fun statusPage(): UIComponent {
        val page = page()
        fun row(label: String, value: () -> String, colorOf: () -> Color = { Colors.TEXT_PRIMARY }) {
            val line = UIContainer().constrain {
                x = 6.pixels()
                y = SiblingConstraint(padding = 6f)
                width = 100.percent() - 12.pixels()
                height = 10.pixels()
            } childOf page
            UIText(label).constrain {
                x = 0.pixels()
                color = Colors.TEXT_SECONDARY.toConstraint()
            } childOf line
            val valueText = UIText(value()).constrain {
                x = 110.pixels()
                color = colorOf().toConstraint()
            } childOf line
            updaters.add {
                valueText.setText(value())
                valueText.setColor(colorOf())
            }
        }
        row("serverify.detail.row.address".localize(), { entry.address() })
        row("serverify.detail.row.status".localize(), { statusLine() }, { if (entry.online()) Colors.ONLINE else Colors.OFFLINE })
        row("serverify.detail.row.ping".localize(), { live?.takeIf { it.online() }?.let { "serverify.detail.ping.value".localize(it.pingMs()) } ?: "-" }, { pingColor() })
        row("serverify.detail.row.version".localize(), { Legacy.plain(entry.version()).ifEmpty { "-" } })
        row("serverify.detail.row.country".localize(), { entry.country().ifEmpty { "-" } })
        row("serverify.detail.row.votes".localize(), { entry.votes().toString() })
        row("serverify.detail.row.source".localize(), { entry.source() })
        return page
    }

    private fun sideBar() {
        Paginator(this).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 160.pixels()
            height = 29.pixels()
        } childOf sideContainer

        val sideBox = UIBlock(Colors.BACKGROUND).constrain {
            x = 0.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 160.pixels()
            height = ChildBasedSizeConstraint() + 4.pixels()
        } childOf sideContainer

        val banner = UIBlock(ServerIcons.color(entry)).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 48.pixels()
        } childOf sideBox
        ServerIcons.image(entry).thenAccept {
            Window.enqueueRenderOperation { banner.setColor(ServerIcons.color(entry)) }
        }

        ServerIcons.icon(entry).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 64.pixels()
            height = 64.pixels()
        } childOf sideBox

        UIWrappedText(Legacy.plain(entry.name())).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = 4f)
            width = 152.pixels()
            textScale = 1.5f.pixels()
            color = Colors.TEXT_PRIMARY.toConstraint()
        } childOf sideBox

        val statusText = UIText(statusLine()).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = 4f)
            color = Colors.TEXT_SECONDARY.toConstraint()
        } childOf sideBox
        val pingText = UIText(pingLine()).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = 2f)
            color = Colors.TEXT_SECONDARY.toConstraint()
        } childOf sideBox
        updaters.add {
            statusText.setText(statusLine())
            pingText.setText(pingLine())
            pingText.setColor(pingColor())
        }

        if (entry.tags().isNotEmpty()) {
            UIText("serverify.detail.tags".localize()).constrain {
                x = 4.pixels()
                y = SiblingConstraint(padding = 8f)
                color = Colors.TEXT_SECONDARY.toConstraint()
            } childOf sideBox
            entry.tags().forEach { tag ->
                UIText("- $tag").constrain {
                    x = 4.pixels()
                    y = SiblingConstraint(padding = 2f)
                    color = Colors.TEXT_SECONDARY.toConstraint()
                } childOf sideBox
            }
        }

        UIText("serverify.card.via".localize(entry.source())).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = 8f)
            color = Colors.TEXT_SECONDARY.toConstraint()
        } childOf sideBox

        actionButton(sideBox, "serverify.detail.add".localize(), firstOfGroup = true) { label, button ->
            val added = MultiplayerIntegration.addToList(UMinecraft.getMinecraft(), entry)
            label.setText((if (added) "serverify.detail.added" else "serverify.detail.already").localize())
            button.setColor(Colors.BUTTON_SECONDARY_DISABLED)
        }
        actionButton(sideBox, "serverify.detail.copy".localize()) { label, _ ->
            UDesktop.setClipboardString(entry.address())
            label.setText("serverify.detail.copied".localize())
        }
        actionButton(sideBox, favoriteLabel()) { label, _ ->
            Serverify.favorites.toggle(entry)
            label.setText(favoriteLabel())
        }
        actionButton(sideBox, "serverify.detail.refresh".localize()) { _, _ -> refreshStatus() }
    }

    private fun favoriteLabel(): String =
        (if (Serverify.favorites.contains(entry)) "serverify.detail.unfavorite" else "serverify.detail.favorite").localize()

    private fun actionButton(
        parent: UIComponent,
        text: String,
        firstOfGroup: Boolean = false,
        onClick: (UIText, UIBlock) -> Unit
    ) {
        val label = UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            color = Colors.TEXT_PRIMARY.toConstraint()
        }
        val button = UIBlock(Colors.BUTTON_SECONDARY).constrain {
            x = 4.pixels()
            y = SiblingConstraint(padding = if (firstOfGroup) 10f else 4f)
            width = 100.percent() - 8.pixels()
            height = 18.pixels()
        } childOf parent
        button.onMouseEnter {
            if (button.getColor() != Colors.BUTTON_SECONDARY_DISABLED) {
                button.animate { setColorAnimation(Animations.OUT_QUAD, 0.1f, HOVER_BUTTON.toConstraint()) }
            }
        }
        button.onMouseLeave {
            if (button.getColor() != Colors.BUTTON_SECONDARY_DISABLED) {
                button.animate { setColorAnimation(Animations.OUT_QUAD, 0.1f, Colors.BUTTON_SECONDARY.toConstraint()) }
            }
        }
        button.onMouseClick {
            if (it.mouseButton != 0) return@onMouseClick
            onClick(label, button)
        }
        label childOf button
    }

    private fun refreshStatus() {
        if (checking) return
        checking = true
        failed = false
        updaters.forEach { it() }
        livePing.ping(entry).whenComplete { result, error ->
            Window.enqueueRenderOperation {
                checking = false
                if (error != null) {
                    failed = true
                } else {
                    live = result
                    entry = entry.withStatus(
                        result.online(), result.players(), result.maxPlayers(),
                        if (result.online()) result.version() else entry.version()
                    )
                }
                updaters.forEach { it() }
            }
        }
    }

    override fun onTick() {
        super.onTick()
        livePing.tick()
        if (++tickCounter % (Serverify.config.refreshSeconds * TICKS_PER_SECOND) == 0) refreshStatus()
    }

    override fun onScreenClose() {
        livePing.close()
        super.onScreenClose()
    }

    private companion object {
        const val GOOD_PING_MS = 150
        const val OK_PING_MS = 300
        const val TICKS_PER_SECOND = 20
        val HOVER_BUTTON = Color(0x9a, 0x9c, 0xaa, 0xff)
    }
}
