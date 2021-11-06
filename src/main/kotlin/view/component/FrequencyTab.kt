package view.component

import controller.EngineController
import javafx.event.EventType
import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import processing.FreqProcessRange
import processing.FreqProcessType
import processing.frequency.ButterworthFilter
import processing.frequency.GaussianFilter
import processing.frequency.IdleFreqFilter
import tornadofx.*
import view.CssStyle

class FrequencyTab(
    private val engineController: EngineController) : VBox() {

    init {
        label("Frequency Filtering ") {
            vboxConstraints {
                margin = Insets(20.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        // type select combobox
        val filterType = combobox(values = FreqProcessType.values().toList())
        this.children.add(HBox(label("filter type ") {addClass(CssStyle.labelTag)}, filterType))

        // range select combobox
        val filterRange = combobox(values = FreqProcessRange.values().toList())
        this.children.add(HBox(label("filter range") {addClass(CssStyle.labelTag)}, filterRange))


        // slider for adjusting filter parameters, initialize to invisible
        val passStopBoundSlider =
            SliderWithSpinner(0.0, 1.0, ChangeListener { _, _, _ -> }, 0.01)
                .withLabel("Cutoff Frequency")
        val bandWidthSlider =
            SliderWithSpinner(0.0, 1.0, ChangeListener { _, _, _ -> }, 0.01)
                .withLabel("Band Width")

        // display bandwidth sliders only when required by the selected filter type/range
        filterRange.valueProperty().addListener(ChangeListener { _, _, rangeName ->
            bandWidthSlider.isVisible = rangeName == FreqProcessRange.BandPass || rangeName == FreqProcessRange.BandReject
        })

        this.children.add(passStopBoundSlider.build())
        this.children.add(bandWidthSlider.build())


        // input box for butterworth order
        val orderSpinner = spinner(
            min = 0.0,
            max = 10.0,
            initialValue = 0.0,
            amountToStepBy = 1.0,
            editable = true
        )
        val orderBox = HBox(
            label("order"),
            orderSpinner
        )
        this.children.add(orderBox)

        // display order input box only when type is butterworth
        filterType.valueProperty().addListener(ChangeListener { _, _, typeName ->
            orderBox.isVisible = typeName == FreqProcessType.ButterWorth
        })

        buttonbar {
            padding = Insets(20.0, 10.0, 20.0, 10.0)
            button("Adjust").setOnAction {
                val bandWidth = bandWidthSlider.getSlider().value
                val passStopBound = passStopBoundSlider.getSlider().value

                // act only when operation is selected
                if (filterType.value != null && filterRange.value != null) {

                    // construct the corresponding filter
                    val filter = when (filterType.value) {
                        FreqProcessType.Idle -> IdleFreqFilter(filterRange.value, passStopBound, bandWidth)
                        FreqProcessType.Gaussian -> GaussianFilter(filterRange.value, passStopBound, bandWidth)
                        FreqProcessType.ButterWorth -> ButterworthFilter(
                            filterRange.value,
                            passStopBound,
                            bandWidth,
                            order = orderSpinner.value.toInt()
                        )
                    }
                    engineController.frequencyTransfer(filter)
                }
            }
            button("Reset").setOnAction {
                filterType.value = null
                filterRange.value = null
                bandWidthSlider.getSlider().value = 0.0
                passStopBoundSlider.getSlider().value = 0.0
            }
        }
    }
}