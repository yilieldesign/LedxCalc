import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.eliezercruz.ledxcalc.LedxCalcApp
import com.eliezercruz.ledxcalc.platform.createPlatformContext
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    document.getElementById("loading")?.remove()
    ComposeViewport(document.getElementById("ComposeTarget")!!) {
        LedxCalcApp(platformContext = createPlatformContext())
    }
}
