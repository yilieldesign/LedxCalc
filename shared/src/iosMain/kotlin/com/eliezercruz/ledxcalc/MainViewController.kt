package com.eliezercruz.ledxcalc

import androidx.compose.ui.window.ComposeUIViewController
import com.eliezercruz.ledxcalc.platform.PlatformContext
import com.eliezercruz.ledxcalc.platform.createPlatformContext

fun MainViewController() = ComposeUIViewController {
    LedxCalcApp(platformContext = createPlatformContext())
}
