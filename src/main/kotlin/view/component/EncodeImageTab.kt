package view.component

import controller.EngineController
import controller.FileController
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.image.WritableImage
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import models.EngineModel
import tornadofx.*

class EncodeImageTab(fileController: FileController, engine: EngineModel, engineController: EngineController): HBox() {

    private var bits = 4
    private var key = ""
    private var isByPixelOrder = false
    private var isIncludedInMetadata = false
    private var hasUndone = false

    init {
        hbox {
            vbox {
                label("Encode/Decode Image") {
                    vboxConstraints {
                        margin = Insets(10.0, 20.0, 0.0, 20.0)
                    }
                    style {
                        fontWeight = FontWeight.BOLD
                        fontSize = Dimension(20.0, Dimension.LinearUnits.px)
                    }
                }
                hbox {
                    imageview(engine.encodeImage) {
                        isPreserveRatio = true
                        fitWidth = 200.0
                        fitHeight = 200.0
                        hboxConstraints {
                            margin = Insets(20.0)
                        }
                    }
                    vbox {
                        hbox {
                            button("Upload") {
                                action {
                                    Utils.imageOperation(mode = "encode", fileController)
                                }
                            }
                            label("  an image and encode with the options below") {
                                hboxConstraints {
                                    marginTop = 5.0
                                    marginBottom = 20.0
                                }
                            }
                        }
                        checkbox("with a random key") {
                            action {
                                key = if (this.isSelected) "randomsequence!" else ""
                            }
                        }
                        hbox {
                            label("with the  ") {
                                hboxConstraints {
                                    marginTop = 5.0
                                }
                            }
                            combobox(values = listOf(1, 2, 3, 4)) {
                                valueProperty()
                                    .addListener(ChangeListener { _, _, new ->
                                        bits = new
                                    })

                                value = 4
                            }
                            label("  lower bits encoded") {
                                hboxConstraints {
                                    marginTop = 5.0
                                }
                            }
                        }
                        hbox {
                            label("encode by the  ") {
                                hboxConstraints {
                                    marginTop = 5.0
                                }
                            }
                            combobox(values = listOf("dimension", "pixel order")) {
                                valueProperty()
                                    .addListener(ChangeListener { _, _, new ->
                                        isByPixelOrder = new != "dimension"
                                    })
                                value = "dimension"
                            }
                            label("  of the encode image") {
                                hboxConstraints {
                                    marginTop = 5.0
                                }
                            }
                        }
                        checkbox("store encoding information to image metadata") {
                            action {
                                isIncludedInMetadata = this.isSelected
                            }
                        }
                        buttonbar {
                            vboxConstraints {
                                marginTop = 20.0
                            }
                            button("Encode") {
                                action {
                                    hasUndone = false
                                    engineController.encodeImage(engine.encodeImage.value, key, bits, isByPixelOrder)
                                }
                            }
                            button("Undo") {
                                action {
                                    hasUndone = true
                                    if (!hasUndone) fileController.undo()
                                }
                            }
                            button("Decode")
                        }
                    }
                }
            }
        }
    }
}