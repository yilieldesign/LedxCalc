package com.eliezercruz.ledxcalc.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/** Oculta el teclado y quita el foco del campo activo. */
expect fun dismissNativeKeyboard()

@Composable
fun rememberKeyboardDismissHandler(): () -> Unit {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
            dismissNativeKeyboard()
        }
    }
}
