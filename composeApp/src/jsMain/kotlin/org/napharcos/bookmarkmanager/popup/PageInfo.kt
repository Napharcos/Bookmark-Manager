package org.napharcos.bookmarkmanager.popup

import org.napharcos.bookmarkmanager.chrome

data class PageInfo(
    val fullUrl: String,
    val baseDomain: String,
    val title: String,
    val favicons: Array<String>,
    val metaImages: Array<String>,
    val pageImages: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as PageInfo

        if (fullUrl != other.fullUrl) return false
        if (baseDomain != other.baseDomain) return false
        if (title != other.title) return false
        if (!favicons.contentEquals(other.favicons)) return false
        if (!metaImages.contentEquals(other.metaImages)) return false
        if (!pageImages.contentEquals(other.pageImages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fullUrl.hashCode()
        result = 31 * result + baseDomain.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + favicons.contentHashCode()
        result = 31 * result + metaImages.contentHashCode()
        result = 31 * result + pageImages.contentHashCode()
        return result
    }
}

fun onPopupOpen(callback: (dynamic) -> Unit) {
    chrome.tabs.query(js("{active: true, currentWindow: true}")) {
        val tabId = it[0].id as Int

        val injection = js("{}")
        injection.target = js("{}")
        injection.target.tabId = tabId
        injection.files = arrayOf("content.js")

        chrome.scripting.executeScript(injection)

        val listener: (dynamic, dynamic, (Any?) -> Unit) -> Unit = { message, _, _ ->
            if (message.type == "PAGE_INFO") {
                callback(message)
            }
        }
        chrome.runtime.onMessage.addListener(listener)
    }
}