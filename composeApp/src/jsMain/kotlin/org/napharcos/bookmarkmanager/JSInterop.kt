package org.napharcos.bookmarkmanager

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

external class TextDecoder(encoding: String = definedExternally) {
    fun decode(input: dynamic): String
}

external interface ReadableStreamDefaultReader<T> {
    fun read(): dynamic
    fun releaseLock()
}

external interface ReadResult<T> {
    val done: Boolean
    val value: T
}

external interface ReadableStream<T> {
    fun getReader(): ReadableStreamDefaultReader<T>
}

external interface Blob {
    fun stream(): ReadableStream<Uint8Array>
}

external interface FileSystemWritableFileStream {
    fun write(data: dynamic): Promise<Unit>
    fun close(): Promise<Unit>
}

external interface FileSystemHandle {
    val kind: String
    val name: String
    fun isSameEntry(other: FileSystemHandle): Promise<Boolean>
}


external interface FileSystemFileHandle : FileSystemHandle {
    fun createWritable(): Promise<dynamic>
    fun createWritable(options: dynamic): Promise<FileSystemWritableFileStream>
    fun remove(): Promise<Unit>
}

external interface FileSystemDirectoryHandle : FileSystemFileHandle {
    fun getFileHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemFileHandle>

    fun getDirectoryHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemDirectoryHandle>

    fun removeEntry(name: String, options: dynamic = definedExternally): Promise<Unit>

    fun resolve(possibleDescendant: FileSystemHandle): Promise<Array<String>?>

    fun keys(): Promise<Array<String>>

    fun values(): Promise<Array<FileSystemHandle>>

    fun entries(): Promise<Array<Array<dynamic>>>

    @JsName("values")
    fun valuesIterator(): dynamic
}

external fun showDirectoryPicker(): Promise<FileSystemDirectoryHandle>

suspend fun FileSystemFileHandle.deleteFile() {
    this.remove().await()
}

fun FileSystemDirectoryHandle.listFiles(): List<FileSystemFileHandle> {
    val files = mutableListOf<FileSystemFileHandle>()
    val iterator = this.valuesIterator()
    var result = iterator.next()
    while (!result.done as Boolean) {
        val handle = result.value
        if ((handle.asDynamic()).kind == "file") {
            files.add(handle.unsafeCast<FileSystemFileHandle>())
        }
        result = iterator.next()
    }
    return files
}

suspend fun FileSystemDirectoryHandle.existsFolder(name: String): Boolean {
    return try {
        this.getDirectoryHandle(name, js("{ create: false }")).await()
        true
    } catch (_: Throwable) { false }
}

suspend fun FileSystemDirectoryHandle.writeFile(name: String, content: String, keep: Boolean) {
    val fileHandle = this.getFileHandle(name, js("{ create: true }")).await()
    val writable = if (keep) fileHandle.createWritable(js("{ keepExistingData: true }")).await() else fileHandle.createWritable().await()
    writable.write(content).await()
    writable.close().await()
}

suspend fun FileSystemDirectoryHandle.isValid(): Boolean {
    return try {
        values().await()
        true
    } catch (_: Throwable) { false }
}