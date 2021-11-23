package view

import javafx.geometry.*
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.input.ZoomEvent
import javafx.scene.layout.StackPane
import models.EngineModel
import tornadofx.*
import view.component.customSpinner

const val WINDOW_WIDTH = 600.0
const val BODY_WIDTH = WINDOW_WIDTH + 100.0

class ImagePanel : View() {
    private val engine: EngineModel by inject()
    private var lastMousePoint: Point2D? = null

    lateinit var oriView: ImageView
    lateinit var newView: ImageView

    lateinit var horizontalSlider: Slider
    lateinit var verticalSlider: Slider
    lateinit var horizontalSpinner: Spinner<Number>
    lateinit var verticalSpinner: Spinner<Number>

    lateinit var stack: StackPane

    // The region of (TL, TR, BL, BR) to show the position of the original image in parallel view.
    lateinit var comparePosition: String

    // Maximum range the left/top pixel coordinate can take,
    // calculated as Image height/width - viewport height/width
    private var excessWidth = 0.0
    private var excessHeight = 0.0

    init {
        engine.addImagePanel(this)
    }

    override val root = vbox {
        vboxConstraints {
            margin = Insets(10.0)
            spacing = 10.0
        }
        alignment = Pos.CENTER
        minWidth = BODY_WIDTH
        comparePosition = "TR"
        hbox {
            hboxConstraints {
                margin = Insets(10.0)
                spacing = 10.0
            }
            alignment = Pos.CENTER
            stack = stackpane {
                oriView = imageview(engine.originalImage) {
                    isVisible = false
                    isPreserveRatio = true
                }
                newView = imageview(engine.parallelImage)

                val viewport = Rectangle2D(
                    .0,
                    .0,
                    oriView.image.width,
                    oriView.image.height,
                )
                updateViewPort(viewport)

                var dragging = false
                this.addEventHandler(MouseEvent.ANY) {
                    if (it.eventType == MouseEvent.MOUSE_PRESSED) {
                        dragging = false;
                    } else if (it.eventType == MouseEvent.DRAG_DETECTED) {
                        dragging = true;
                    } else if (it.eventType == MouseEvent.MOUSE_CLICKED) {
                        if (!dragging) {
                            oriView.isVisible = !oriView.isVisible
                            newView.isVisible = !newView.isVisible
                        }
                    }
                }

                // listen to zoomProperty to detect zoom in & out action
                // Use ctrl+scroll in Windows as compatibility issue occurs for zoom
                // Use scroll only to move around image
                this.addEventFilter(ScrollEvent.SCROLL) {
                    //this.addEventFilter(KeyEvent.KEY_PRESSED) {
                    if (it.isControlDown) {
                        zoom(it.deltaY, it.x, it.y)
                    } else {
                        var leftTopX = oriView.viewport.minX - it.deltaX
                        leftTopX = cast(leftTopX, 0.0, excessWidth)
                        var leftTopY = oriView.viewport.minY - it.deltaY
                        leftTopY = cast(leftTopY, 0.0, excessHeight)
                        val viewport = Rectangle2D(
                            leftTopX,
                            leftTopY,
                            oriView.viewport.width,
                            oriView.viewport.height,
                        )
                        updateViewPort(viewport)
                    }
                }

                // Zoom event for Mac users
                this.addEventFilter(ZoomEvent.ANY) {
                    zoom(it.zoomFactor, it.x, it.y)
                }

                // Drag image handlers
                fun stopDrag(event: MouseEvent) {
                    lastMousePoint = null
                }
                addEventFilter(MouseEvent.MOUSE_RELEASED, ::stopDrag)
                addEventFilter(MouseEvent.MOUSE_PRESSED) {
                    lastMousePoint = Point2D(it.screenX, it.screenY)
                }

                addEventFilter(MouseEvent.MOUSE_DRAGGED) {
                    if (lastMousePoint != null) {
                        val oldViewport = oriView.viewport

                        var leftTopX =
                            oldViewport.minX - localToImage(it.screenX - lastMousePoint!!.x)
                        leftTopX = cast(leftTopX, 0.0, excessWidth)
                        var leftTopY =
                            oldViewport.minY - localToImage(it.screenY - lastMousePoint!!.y)
                        leftTopY = cast(leftTopY, 0.0, excessHeight)

                        val newViewport = Rectangle2D(
                            leftTopX,
                            leftTopY,
                            oldViewport.width,
                            oldViewport.height,
                        )
                        updateViewPort(newViewport)

                        lastMousePoint = Point2D(it.screenX, it.screenY)
                    }
                }
            }

            // TODO: Better way to toggle between the images
            stack.children.forEach { child ->
                run {
                    if (child is ImageView) {
                        child.fitWidth = WINDOW_WIDTH
                        child.isPreserveRatio = true
                    }
                }
            }

            this.minWidth = WINDOW_WIDTH
            this.maxWidth = WINDOW_WIDTH
            this.minHeight = WINDOW_WIDTH / oriView.image.width * oriView.image.height
            this.maxHeight = WINDOW_WIDTH / oriView.image.width * oriView.image.height

            // The slider at the side of the image view to slide between new and original image.
            verticalSlider = slider {
                maxHeight = oriView.image.height * 1.2
                min = 0.0
                max = oriView.image.width
                blockIncrement = 1.0
                orientation = Orientation.VERTICAL
            }

            verticalSlider.value = verticalSlider.max
            verticalSlider.valueProperty().addListener(ChangeListener { _, _, new ->
                engine.parallelView(horizontalSlider.value, new.toDouble(), comparePosition)
            })

//            verticalSpinner = customSpinner(
//                min = .0,
//                max = Double.MAX_VALUE,
//                amountToStepBy = 1.0,
//                editable = true,
//                property = doubleProperty(verticalSlider.max),
//                type = "int"
//            ) {
//                maxWidth = 70.0
//                minWidth = 70.0
//            }
//
//            try {
//                verticalSlider.valueProperty().bindBidirectional(
//                    verticalSpinner.valueFactory.valueProperty()
//                )
//            } catch (e: NumberFormatException) {
//            }
//
//            this.add(verticalSpinner)

        }
        // The slider at the bottom of the image view to slide between new and original image.
        vbox {
            vboxConstraints {
                marginLeft = 35.0
            }
            horizontalSlider = slider {
                maxWidth = WINDOW_WIDTH
                min = 0.0
                max = oriView.image.width
                blockIncrement = 1.0
            }

            horizontalSlider.value = horizontalSlider.max
            horizontalSlider.valueProperty().addListener(ChangeListener { _, _, new ->
                engine.parallelView(new.toDouble(), verticalSlider.value, comparePosition)
            })
        }

//        horizontalSpinner = customSpinner(
//            min = .0,
//            max = Double.MAX_VALUE,
//            amountToStepBy = 1.0,
//            editable = true,
//            property = doubleProperty(horizontalSlider.max),
//            type = "int"
//        ) {
//            maxWidth = 70.0
//            minWidth = 70.0
//        }
//
//        try {
//            horizontalSlider.valueProperty().bindBidirectional(
//                horizontalSpinner.valueFactory.valueProperty()
//            )
//        } catch (e: NumberFormatException) {
//        }

        hbox {
            alignment = Pos.CENTER
            spacing = 10.0
//            this.add(horizontalSpinner)

            vbox {
                alignment = Pos.CENTER
                spacing = 10.0
                togglegroup {
                    hbox {
                        alignment = Pos.CENTER
                        // TL
                        radiobutton("Show Top Left") {
                            toggleGroup = this.parent.parent.getToggleGroup()
                            spacing = 5.0
                            this.selectedProperty().addListener(ChangeListener { _, _, _ ->
                                if (isSelected) {
                                    comparePosition = "TL"
                                    engine.parallelView(
                                        horizontalSlider.value,
                                        verticalSlider.value,
                                        comparePosition
                                    )
                                }
                            })
                        }
                        // TR
                        radiobutton("Show Top Right") {
                            isSelected = true
                            toggleGroup = this.parent.parent.getToggleGroup()
                            spacing = 5.0
                            this.selectedProperty().addListener(ChangeListener { _, _, _ ->
                                if (isSelected) {
                                    comparePosition = "TR"
                                    engine.parallelView(
                                        horizontalSlider.value,
                                        verticalSlider.value,
                                        comparePosition
                                    )
                                }
                            })
                        }
                    }
                    hbox {
                        alignment = Pos.CENTER
                        // BL
                        radiobutton("Show Bottom Left") {
                            toggleGroup = this.parent.parent.getToggleGroup()
                            spacing = 5.0
                            this.selectedProperty().addListener(ChangeListener { _, _, _ ->
                                if (isSelected) {
                                    comparePosition = "BL"
                                    engine.parallelView(
                                        horizontalSlider.value,
                                        verticalSlider.value,
                                        comparePosition
                                    )
                                }
                            })
                        }
                        // BR
                        radiobutton("Show Bottom Right") {
                            toggleGroup = this.parent.parent.getToggleGroup()
                            spacing = 5.0
                            this.selectedProperty().addListener(ChangeListener { _, _, _ ->
                                if (isSelected) {
                                    comparePosition = "BR"
                                    engine.parallelView(
                                        horizontalSlider.value,
                                        verticalSlider.value,
                                        comparePosition
                                    )
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    // Zoom the image
    private fun zoom(factor: Double, x: Double, y: Double) {
        var ratio = 1.0
        val oldViewport = oriView.viewport!!
        if (factor > 1) {
            ratio = 1.035
        } else if (factor < 1) {
            // only zoom out when viewport is smaller than original image
            if (oldViewport.width < oriView.image.width) {
                ratio = 1 / 1.035
            }
        }

        // update image origin so zoom on the mouse position
        var leftTopX = oldViewport.minX + localToImage(x * (1 - 1 / ratio))
        leftTopX = cast(leftTopX, 0.0, excessWidth)
        var leftTopY = oldViewport.minY + localToImage(y * (1 - 1 / ratio))
        leftTopY = cast(leftTopY, 0.0, excessHeight)

        val newViewport = Rectangle2D(
            leftTopX,
            leftTopY,
            oldViewport.width / ratio,
            oldViewport.height / ratio,
        )
        updateViewPort(newViewport)
    }

    fun sliderInit() {
        horizontalSlider.value = horizontalSlider.max
        verticalSlider.value = verticalSlider.max
    }

    fun updateSlider(newMaxWidth: Double, newMaxHeight: Double) {
        stack.minHeight = WINDOW_WIDTH / oriView.image.width * oriView.image.height
        stack.maxHeight = WINDOW_WIDTH / oriView.image.width * oriView.image.height

        horizontalSlider.max = newMaxWidth
        verticalSlider.max = newMaxHeight
        verticalSlider.maxHeight = WINDOW_WIDTH / oriView.image.width * oriView.image.height

        horizontalSlider.valueProperty()
            .unbindBidirectional(horizontalSpinner.valueFactory.valueProperty())
        verticalSlider.valueProperty()
            .unbindBidirectional(verticalSpinner.valueFactory.valueProperty())
        @Suppress("UNCHECKED_CAST")
        verticalSpinner.valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(
            verticalSlider.min,
            verticalSlider.max,
            verticalSlider.blockIncrement
        ) as SpinnerValueFactory<Number>
        @Suppress("UNCHECKED_CAST")
        verticalSpinner.valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(
            verticalSlider.min,
            verticalSlider.max,
            verticalSlider.blockIncrement
        ) as SpinnerValueFactory<Number>
        try {
            horizontalSlider.valueProperty().bindBidirectional(
                horizontalSpinner.valueFactory.valueProperty()
            )
            verticalSlider.valueProperty().bindBidirectional(
                verticalSpinner.valueFactory.valueProperty()
            )
        } catch (e: NumberFormatException) {
        }
    }

    // cast given value in given range
    private fun cast(value: Double, min: Double, max: Double): Double {
        if (value < min) {
            return min
        } else if (value > max) {
            return max
        }
        return value
    }

    // update both images' viewport in engine
    fun updateViewPort(viewPort: Rectangle2D) {
        oriView.viewport = viewPort
        newView.viewport = viewPort
        excessWidth = oriView.image.width - viewPort.width
        excessHeight = oriView.image.height - viewPort.height
    }

    // from scene / screen / stage / view coordinates to image coordinates
    private fun localToImage(value: Double): Double {
        val viewport = oriView.viewport!!
        val localToImage = WINDOW_WIDTH / viewport.width

        return value / localToImage
    }
}

