package org.napharcos.bookmarkmanager

@Suppress("ClassName")
external object chrome {
    val i18n: I18n
    val tabs: Tabs
    val runtime: Runtime
    val scripting: Scripting
    val action: Action
}

external interface Action {
    fun setIcon(details: SetIconDetails)
}

external interface SetIconDetails {
    var path: dynamic /* String | IconPathObject */
    var tabId: Int?
}

external interface I18n {
    fun getMessage(messageName: String): String
    fun getMessage(messageName: String, substitutions: String): String
    fun getMessage(messageName: String, substitutions: Array<String>): String
}

external interface Tabs {
    fun query(query: dynamic, callback: (Array<dynamic>) -> Unit)
    fun captureVisibleTab(windowId: Int? = definedExternally, options: CaptureOptions, callback: (String) -> Unit)
}

external interface CaptureOptions {
    var format: String?
}

external interface Runtime {
    val onMessage: OnMessageEvent
}

external interface OnMessageEvent {
    fun addListener(callback: (message: dynamic, sender: dynamic, sendResponse: (Any?) -> Unit) -> Unit)
}

external interface Scripting {
    fun executeScript(injection: dynamic, callback: ((Array<dynamic>) -> Unit)? = definedExternally)
}

fun getString(key: String, vararg args: Any): String {
    return when {
        args.isEmpty() -> chrome.i18n.getMessage(key)
        args.size == 1 -> chrome.i18n.getMessage(key, args[0].toString())
        else -> chrome.i18n.getMessage(key, args.map { it.toString() }.toTypedArray())
    }
}