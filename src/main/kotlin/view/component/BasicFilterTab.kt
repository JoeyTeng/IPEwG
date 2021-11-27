package view.component

import controller.EngineController
import javafx.geometry.Insets
import javafx.scene.text.FontWeight
import tornadofx.*

class BasicFilterTab : Fragment("Basic Actions") {

    private val engineController: EngineController by inject()

    private val basicFilterButtonList = mapOf(
        "Inverse Color" to engineController::inverseColour,
        "Greyscale" to engineController::grayscale,
        "Flip Horizontal" to engineController::flipHorizontal,
        "Flip Vertical" to engineController::flipVertical,
        "Edge Detection" to engineController::edgeDetection,
        "Sharpen" to engineController::sharpen
    )

    override val root = vbox {
        label("Basic Actions") {
            vboxConstraints {
                margin = Insets(20.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        hbox {
            padding = Insets(20.0, 20.0, 10.0, 10.0)
            buttonbar {
                basicFilterButtonList.map { (s, callback) ->
                    button(s) {
                        /* The buttons need enough width to load up all labels
                         in them, or the border will change when tabs clicked. */
                        prefWidth = 60.0
                    }.setOnAction { callback.call() }
                }
            }
        }

        label("Rotation") {
            vboxConstraints {
                margin = Insets(10.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        vbox {
            val slider = SliderWithSpinner(0.0, 360.0, ChangeListener { _, _, new ->
                engineController.rotate(new as Double)
            }).withLabel("Rotate Degree")
            this.children.add(slider.build())
            button("Apply Rotation") {
                vboxConstraints {
                    margin = Insets(10.0, 20.0, 10.0, 10.0)
                }
                action {
                    engineController.submitAdjustment()
                }
            }
        }
    }
}