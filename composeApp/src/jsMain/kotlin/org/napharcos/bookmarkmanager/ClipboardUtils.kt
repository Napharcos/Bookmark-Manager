package org.napharcos.bookmarkmanager

import kotlinx.browser.window
import org.w3c.files.FileReader
import kotlin.js.Promise

object ClipboardUtils {

    private val permissions = setOf("image/png", "image/jpg", "image/jpeg")

    private fun getType(types: Array<String>): String? {
        return types.firstOrNull { it in permissions }
    }

    private fun getItem(items: Array<dynamic>): Promise<org.w3c.files.Blob>? {
        for (item in items) {
            val types = item.types.unsafeCast<Array<String>>()
            val type = getType(types)
            if (type != null) {
                return item.getType(type).unsafeCast<Promise<org.w3c.files.Blob>>()
            }
        }
        return null
    }

    private fun loadFile(blob: org.w3c.files.Blob, callback: (String?, String?) -> Unit) {
        val reader = FileReader()
        reader.onload = { callback(reader.result as String, null) }
        reader.onerror = { callback(null, "Incorrect file.") }
        reader.readAsDataURL(blob)
    }

    fun readImage(callback: (dataUrl: String?, error: String?) -> Unit) {
        val clipboard = window.navigator.clipboard

        clipboard.read().then { items ->
            val arrayItems = items.unsafeCast<Array<dynamic>>()
            val promise = getItem(arrayItems)

            promise?.then { blob ->
                loadFile(blob, callback)
            }?.catch {
                callback(null, "Reading clipboard error.")
            } ?: callback(null, null)

        }.catch {
            callback(null, "Reading clipboard error.")
        }
    }
}