// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.gui

import com.serverify.model.ServerEntry
import com.serverify.network.Http
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.Window
import gg.essential.universal.UGraphics
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

object ServerIcons {
    private const val SIZE = 64
    private const val MIN_BRIGHTNESS = 0.35f

    private val images = ConcurrentHashMap<String, CompletableFuture<BufferedImage>>()
    private val colors = ConcurrentHashMap<String, Color>()

    fun image(entry: ServerEntry): CompletableFuture<BufferedImage> =
        images.computeIfAbsent(entry.key()) { load(entry) }

    fun icon(entry: ServerEntry): UIImageWrapper {
        val image = UIImage(CompletableFuture<BufferedImage>(), EmptyImage, EmptyImage)
        image.textureMinFilter = UIImage.TextureScalingMode.LINEAR
        image.textureMagFilter = UIImage.TextureScalingMode.LINEAR
        image(entry).thenAccept { loaded ->
            val texture = UGraphics.getTexture(loaded)
            Window.enqueueRenderOperation {
                texture.uploadTexture()
                image.applyTexture(texture)
            }
        }
        return UIImageWrapper(image)
    }

    fun color(entry: ServerEntry): Color = colors[entry.key()] ?: Colors.DEFAULT_BANNER

    fun clear() {
        images.clear()
        colors.clear()
    }

    private fun load(entry: ServerEntry): CompletableFuture<BufferedImage> =
        fetch(entry.iconUrl())
            .orElse { fetch(fallbackUrl(entry)) }
            .exceptionally { placeholder() }
            .thenApply { image ->
                colors[entry.key()] = dominantColor(image)
                image
            }

    private fun fallbackUrl(entry: ServerEntry): String =
        "https://api.mcsrvstat.us/icon/" + URLEncoder.encode(entry.address(), "UTF-8")

    private fun fetch(url: String): CompletableFuture<BufferedImage> {
        if (url.isEmpty()) return CompletableFuture<BufferedImage>().also { it.completeExceptionally(IOException("no icon url")) }
        return Http.getBytes(url).thenApply { bytes ->
            val source = ImageIO.read(ByteArrayInputStream(bytes)) ?: throw IOException("unsupported image format")
            scaled(source)
        }
    }

    private fun scaled(source: BufferedImage): BufferedImage {
        val out = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(source, 0, 0, SIZE, SIZE, null)
        g.dispose()
        return out
    }

    private fun placeholder(): BufferedImage {
        val out = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        val g = out.createGraphics()
        g.color = Color(0x3a, 0x3f, 0x55)
        g.fillRect(0, 0, SIZE, SIZE)
        g.color = Color(0x55, 0x5c, 0x7a)
        g.fillRect(0, 0, SIZE, 4)
        g.fillRect(0, SIZE - 4, SIZE, 4)
        g.fillRect(0, 0, 4, SIZE)
        g.fillRect(SIZE - 4, 0, 4, SIZE)
        g.dispose()
        return out
    }

    private fun dominantColor(image: BufferedImage): Color {
        var r = 0L
        var g = 0L
        var b = 0L
        var count = 0L
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                if ((argb ushr 24) < 128) continue
                r += (argb shr 16) and 255
                g += (argb shr 8) and 255
                b += argb and 255
                count++
            }
        }
        if (count == 0L) return Colors.DEFAULT_BANNER
        val average = Color((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
        val hsb = Color.RGBtoHSB(average.red, average.green, average.blue, null)
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2].coerceAtLeast(MIN_BRIGHTNESS)))
    }
}

private fun <T> CompletableFuture<T>.orElse(next: () -> CompletableFuture<T>): CompletableFuture<T> =
    handle { value, error -> if (error == null) CompletableFuture.completedFuture(value) else next() }.thenCompose { it }
