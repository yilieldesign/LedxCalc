import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.eliezercruz.ledxcalc.LedxCalcApp
import com.eliezercruz.ledxcalc.platform.createPlatformContext
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    document.getElementById("loading")?.remove()
    ComposeViewport(document.getElementById("ComposeTarget")!!) {
        LedxCalcApp(platformContext = createPlatformContext())
    }
}
