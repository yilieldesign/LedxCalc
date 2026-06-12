package com.eliezercruz.ledxcalc.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLElement

actual fun dismissNativeKeyboard() {
    (document.activeElement as? HTMLElement)?.blur()
}
