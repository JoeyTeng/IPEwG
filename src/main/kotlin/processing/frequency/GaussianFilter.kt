package processing.frequency

import processing.FreqProcessRange
import processing.FreqProcessRange.*
import kotlin.math.exp
import kotlin.math.pow

class GaussianFilter(
    private val range: FreqProcessRange,
    private val passStopBound: Double,
    private val bandWidth: Double)
    : FrequencyFilters() {

    override fun getFilterPixel(dist: Double): Double {
        val baseVal = when (range) {
            LowPass, HighPass -> exp(
                -dist.pow(2)
                        / (2.0 * passStopBound.pow(2)))
            BandPass, BandReject -> exp(
                -(dist.pow(2) - passStopBound.pow(2)).pow(2)
                        / (dist * bandWidth))
        }
        if (range == HighPass || range == BandReject) {
            return 1 - baseVal
        }
        return baseVal
    }

    override fun toString(): String {
        val baseString = "$range gaussian filter, cutoff frequency: ${"%.2f".format(passStopBound)}"
        if (range == BandReject || range == BandPass) {
            return baseString + ", bandwidth: ${"%.2f".format(bandWidth)}"
        }
        return baseString
    }
}