package processing.filters

import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import processing.ImageProcessing
import processing.filters.blur.GaussianBlur
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.sqrt

@Serializable
@SerialName("EdgeDetection")
class EdgeDetection : ImageProcessing {
    override fun process(srcImage: WritableImage, destImage: WritableImage) {
        Grayscale().process(srcImage, destImage)
        GaussianBlur(4).process(destImage, destImage)

        // Apply Sobel operator for basic edge detection
        val horizontalKernel = arrayOf(arrayOf(1.0, 2.0, 1.0), arrayOf(0.0, 0.0, 0.0), arrayOf(-1.0, -2.0, -1.0))
        val horizontal = Convolution(horizontalKernel).convolutionGreyScaleNegative(destImage)

        val verticalKernel = arrayOf(arrayOf(1.0, 0.0, -1.0), arrayOf(2.0, 0.0, -2.0), arrayOf(1.0, 0.0, -1.0))
        val vertical = Convolution(verticalKernel).convolutionGreyScaleNegative(destImage)

        val direction = Array(destImage.width.toInt()) { DoubleArray(destImage.height.toInt()) }
        val grayImage = Array(destImage.width.toInt()) { DoubleArray(destImage.height.toInt()) }

        for (x in 0 until destImage.width.toInt()) {
            for (y in 0 until destImage.height.toInt()) {
                val horizontalPx = horizontal[x][y]
                val verticalPx = vertical[x][y]
                grayImage[x][y] = sqrt(horizontalPx * horizontalPx + verticalPx * verticalPx).coerceIn(0.0, 1.0)
                val degree = toDegrees(atan2(verticalPx, horizontalPx))
                direction[x][y] = if (degree < 0) degree + 180 else degree
            }
        }
        nonMaxSuppression(grayImage, direction)
        thresholdAndEdgeTracking(grayImage)
        writeGrayImage(grayImage, destImage)
    }

    private fun nonMaxSuppression(input: Array<DoubleArray>, direction: Array<DoubleArray>) {
        val original = input.map { it.clone() }.toTypedArray()

        for (x in 1 until original.size - 1) {
            for (y in 1 until original[0].size - 1) {
                var q: Double
                var r: Double
                val angle = direction[x][y]
                when {
                    (0 <= angle && angle < 22.5) -> {
                        q = original[x][y + 1]
                        r = original[x][y - 1]
                    }
                    (22.5 <= angle && angle < 67.5) -> {
                        q = original[x + 1][y - 1]
                        r = original[x - 1][y + 1]
                    }
                    (67.5 <= angle && angle < 112.5) -> {
                        q = original[x + 1][y]
                        r = original[x - 1][y]
                    }
                    (112.5 <= angle && angle < 157.5) -> {
                        q = original[x - 1][y - 1]
                        r = original[x + 1][y + 1]
                    }
                    else -> {
                        q = original[x][y + 1]
                        r = original[x][y - 1]
                    }
                }
                input[x][y] = if (original[x][y] >= q && original[x][y] >= r) input[x][y] else 0.0
            }
        }
    }

    private fun thresholdAndEdgeTracking(
        input: Array<DoubleArray>,
        lowThresholdRatio: Double = 0.05,
        highThresholdRatio: Double = 0.09
    ) {
        val highThreshold = input.maxOf { it.maxOrNull()!! } * highThresholdRatio
        val lowThreshold = highThreshold * lowThresholdRatio

        // Edge tracking, mark strong and weak pixels
        for (x in input.indices) {
            for (y in input[0].indices) {
                if (input[x][y] > highThreshold) input[x][y] = 1.0
                else if (input[x][y] < lowThreshold) input[x][y] = 0.0
                else input[x][y] = 0.5
            }
        }

        val ambiguous = input.map { it.clone() }.toTypedArray()

        // connect pixels in-between strong and weak only if neighbours are strong
        for (x in input.indices) {
            for (y in input[0].indices) {
                if (input[x][y] == 0.5) {
                    input[x][y] = 0.0

                    for (i in x - 1..x + 1) {
                        if (i < 0 || i >= input.size) continue
                        for (j in y - 1..y + 1) {
                            if (j < 0 || j >= input[0].size) continue
                            if (ambiguous[i][j] == 1.0) {
                                input[x][y] = 1.0
                                break
                            }
                        }
                        if (input[x][y] == 1.0) break
                    }
                }
            }
        }
    }

    override fun toString(): String = "Edge detection (Canny)"
}

fun writeGrayImage(input: Array<DoubleArray>, image: WritableImage) {
    for (x in 0 until image.width.toInt()) {
        for (y in 0 until image.height.toInt()) {
            image.pixelWriter.setColor(x, y, Color.color(input[x][y], input[x][y], input[x][y]))
        }
    }
}