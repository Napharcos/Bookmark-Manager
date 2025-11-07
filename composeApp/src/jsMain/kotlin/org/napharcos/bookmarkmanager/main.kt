package org.napharcos.bookmarkmanager

import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.napharcos.bookmarkmanager.options.OptionsSummary
import org.napharcos.bookmarkmanager.popup.PopupSummary
import org.w3c.dom.asList

const val POPUP = "popup"
const val OPTIONS = "options"

fun main() {
    val context = detectContext()

    when (context) {
        POPUP -> renderComposable(rootElementId = "root") { PopupSummary() }
        OPTIONS -> renderComposable(rootElementId = "root") { OptionsSummary() }
    }
}

fun detectContext(): String {
    for (sheet in document.styleSheets.asList()) {
        val href = sheet.asDynamic().href as? String ?: continue
        if (href.contains("$POPUP.css")) return POPUP
        if (href.contains("$OPTIONS.css")) return OPTIONS
    }
    return "unknown"
}