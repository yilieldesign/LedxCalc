import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.eliezercruz.ledxcalc.LedxCalcApp
import com.eliezercruz.ledxcalc.platform.createPlatformContext
import com.eliezercruz.ledxcalc.ui.drawing.SketchFontBootstrap
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    MainScope().launch {
        SketchFontBootstrap.ensureLoaded()
        document.getElementById("loading")?.remove()
        ComposeViewport(document.getElementById("ComposeTarget")!!) {
            LedxCalcApp(platformContext = createPlatformContext())
        }
    }
}
