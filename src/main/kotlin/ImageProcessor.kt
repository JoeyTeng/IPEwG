import tornadofx.App
import tornadofx.View
import tornadofx.borderpane
import tornadofx.launch
import view.CssStyle
import view.FilterPanel
import view.ImagePanel
import view.TopBar

class GUI : View("IPEwG") {
    override val root = borderpane {
        setPrefSize(1100.0, 780.0)
        top<TopBar>()
        left<FilterPanel>()
        center<ImagePanel>()
    }
}

class ImageProcessor : App(GUI::class, CssStyle::class)

fun main(args: Array<String>) {
    launch<ImageProcessor>(args)
}