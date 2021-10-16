package view

import controller.ImageController
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.text.FontWeight
import models.FilterOperation
import tornadofx.*

class FilterPanel : View() {
    val basicFilterButtonList = mapOf(
        "Inverse Color" to FilterOperation.INVERSE_COLOR,
        "Greyscale" to FilterOperation.GREYSCALE,
        "Mirror" to FilterOperation.MIRROR
    )
    val basicFilterSliderList = mapOf(
        "R" to FilterOperation.R,
        "G" to FilterOperation.G,
        "B" to FilterOperation.B,
        "Brightness" to FilterOperation.BRIGHTNESS,
        "Contrast" to FilterOperation.CONTRAST,
        "Saturation" to FilterOperation.SATURATION
    )
    val imageController: ImageController by inject()

    override val root = vbox {
        label("Quick Action") {
            vboxConstraints {
                margin = Insets(20.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        hbox {
            padding = Insets(0.0, 10.0, 0.0, 10.0)
            basicFilterButtonList.map { (s, op) ->
                hbox {
                    addClass(CssStyle.checkBox)
                    checkbox(s) {
                        action {
                            imageController.applyFilter(op, 0.0)
                        }
                    }
                }
            }
        }

        label("Quick Action") {
            vboxConstraints {
                margin = Insets(20.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        vbox {
            vboxConstraints {
                margin = Insets(10.0)
            }

            basicFilterSliderList.map { (label, op) ->
                hbox {
                    label(label) {
                        addClass(CssStyle.labelTag)
                    }
                    val slider = slider {
                        min = 0.0
                        max = 100.0
                    }
                    slider.valueChangingProperty().addListener(object : ChangeListener<Boolean?> {
                        override fun changed(
                            source: ObservableValue<out Boolean?>?,
                            oldValue: Boolean?,
                            newValue: Boolean?
                        ) {
                            println()
                            imageController.applyFilter(op, slider.value / 100.0)
                        }
                    })
                    addClass(CssStyle.filterSlider)
                }
            }
        }
    }
}