package view.component

import controller.EngineController
import controller.FileController
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import models.EngineModel
import processing.filters.BlendType
import tornadofx.*
import java.io.File

class BlendTab(
    private val engine: EngineModel,
    private val engineController: EngineController,
    private val fileController: FileController
) : VBox() {
    init {
        label("Blend") {
            vboxConstraints {
                margin = Insets(20.0, 20.0, 10.0, 10.0)
            }
            style {
                fontWeight = FontWeight.BOLD
                fontSize = Dimension(20.0, Dimension.LinearUnits.px)
            }
        }

        hbox {
            padding = Insets(20.0, 10.0, 20.0, 10.0)

            val importButton = button("Import")

            val blendView = imageview(engine.blendImage) {
                isPreserveRatio = true
            }
            blendView.fitHeight = 100.0
            blendView.fitWidth = 200.0
            importButton.setOnAction {
                importBlendImage()
            }
        }

        hbox {
            padding = Insets(20.0, 10.0, 20.0, 10.0)

            val blendList = BlendType.values().toList()
            val comboBox = combobox(values = blendList)
            comboBox.value = blendList[0]

            button("Blend").setOnAction {
                engineController.blend(comboBox.value)
            }
        }
    }

    private fun importBlendImage() {
        val importFilter = arrayOf(
            FileChooser.ExtensionFilter(
                "PNG files (*.png)",
                "*.png"
            ),
            FileChooser.ExtensionFilter(
                "Bitmap files (*.bmp)",
                "*.bmp"
            ),
            FileChooser.ExtensionFilter(
                "JPEG files (*.jpeg, *.jpg)",
                "*.jpeg",
                "*.jpg"
            )
        )

        try {
            val fileSelectorTitle = "Import image"
            val fileSelectorMode = FileChooserMode.Single

            val dir = chooseFile(
                title = fileSelectorTitle,
                filters = importFilter,
                mode = fileSelectorMode
            ) {
                initialDirectory = File(File("").canonicalPath)
                initialFileName = "IPEwG_result_image"
            }
            if (dir.isNotEmpty()) {
                fileController.loadBlendImage("file:///" + dir[0].toString())
            }

        } catch (e: IllegalArgumentException) {
            alert(
                type = Alert.AlertType.ERROR,
                header = "Invalid image path",
                content = "The image path you entered is incorrect.\n" +
                        "Please check!" + e.toString()
            )
        }
    }
}