package processing.filters

import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import processing.ImageProcessing
import processing.filters.BlendType.*

@Serializable
@SerialName("Blend")
class Blend(@property:Contextual val blendImage: Image, val mode: BlendType) :
    ImageProcessing {
    override fun process(image: WritableImage) {
        val width = image.width.toInt()
        val height = image.height.toInt()
        val readerA: PixelReader = image.pixelReader
        val readerB: PixelReader = blendImage.pixelReader
        val writer: PixelWriter = image.pixelWriter
        for (y in 0 until height) {
            for (x in 0 until width) {
                val oldColorA = readerA.getColor(x, y)
                val oldColorB = readerB.getColor(x, y)
                val newR = getChannelOperation()(oldColorA.red, oldColorB.red)
                val newG = getChannelOperation()(oldColorA.green, oldColorB.green)
                val newB = getChannelOperation()(oldColorA.blue, oldColorB.blue)
                val newColor = Color(newR, newG, newB, oldColorA.opacity)
                writer.setColor(x, y, newColor)
            }
        }
    }

    fun getChannelOperation(): (Double, Double) -> Double {
        return when (mode) {
            NORMAL -> { a, _ -> a }
            MULTIPLY -> { a, b -> a * b }
            SCREEN -> { a, b -> 1 - (1 - a) * (1 - b) }
            OVERLAY -> TODO()
            DARKEN -> { a, b -> a.coerceAtMost(b) }
            LIGHTEN -> { a, b -> a.coerceAtLeast(b) }
            COLOR_DODGE -> TODO()
            COLOR_BURN -> TODO()
            HARD_LIGHT -> TODO()
            SOFT_LIGHT -> TODO()
            DIFFERENCE -> TODO()
            EXCLUSION -> TODO()
            HUE -> TODO()
            SATURATION -> TODO()
            COLOR -> TODO()
            LUMINOSITY -> TODO()
        }
    }
}