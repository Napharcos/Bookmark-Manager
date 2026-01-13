package org.napharcos.bookmarkmanager

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.w3c.files.File
import kotlin.js.Promise

external class TextDecoder(encoding: String = definedExternally) {
    fun decode(input: dynamic): String
}

external interface ReadableStreamDefaultReader<T> {
    fun read(): dynamic
}

external interface ReadableStream<T> {
    fun getReader(): ReadableStreamDefaultReader<T>
}

external interface Blob {
    fun stream(): ReadableStream<Uint8Array>
}

external interface FileSystemWritableFileStream {
    fun write(data: dynamic): Promise<Unit>
    fun seek(position: Int): Promise<Unit>
    fun close(): Promise<Unit>
}

external interface FileSystemHandle {
    val name: String

    fun queryPermission(
        descriptor: FileSystemHandlePermissionDescriptor = definedExternally
    ): Promise<String>

    fun requestPermission(
        descriptor: FileSystemHandlePermissionDescriptor = definedExternally
    ): Promise<String> /* "granted" | "denied" */
}


external interface FileSystemFileHandle : FileSystemHandle {
    fun createWritable(): Promise<dynamic>
    fun createWritable(options: dynamic): Promise<FileSystemWritableFileStream>
    fun remove(): Promise<Unit>
    fun getFile(): Promise<File>
}

external interface FileSystemDirectoryHandle : FileSystemFileHandle {
    fun getFileHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemFileHandle>

    fun getDirectoryHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemDirectoryHandle>

    fun removeEntry(name: String, options: dynamic = definedExternally): Promise<Unit>
    fun values(): dynamic
}

external interface FileSystemHandlePermissionDescriptor {
    var mode: String?
}

external fun showDirectoryPicker(): Promise<FileSystemDirectoryHandle>

private val asyncIteratorSymbol: dynamic = js("Symbol.asyncIterator")

suspend fun FileSystemDirectoryHandle.listFiles(): List<FileSystemFileHandle> {
    val files = mutableListOf<FileSystemFileHandle>()

    val iterable = values()
    val iterator = iterable[asyncIteratorSymbol]()

    while (true) {
        val result = iterator.next().unsafeCast<Promise<dynamic>>().await()
        if (result.done as Boolean) break

        val handle = result.value
        if (handle != null && handle.kind == "file") {
            files.add(handle.unsafeCast<FileSystemFileHandle>())
        }
    }

    return files
}


suspend fun FileSystemDirectoryHandle.writeFile(
    name: String,
    content: String,
    keep: Boolean = false
) {
    val fileHandle = getFileHandle(name, js("{ create: true }")).await()

    val writable = if (keep) fileHandle.createWritable(js("{ keepExistingData: true }")).await()
        else fileHandle.createWritable().await()

    if (keep) {
        val file = fileHandle.getFile().await()
        writable.seek(file.size.toInt())
    }

    (writable.write(content) as Promise<Unit>).await()
    (writable.close() as Promise<Unit>).await()
}

suspend fun FileSystemDirectoryHandle.isWritable(): Boolean {
    val permission = this.requestPermission(js("{ mode: 'readwrite' }")).await()
    return permission == "granted"
}

suspend fun FileSystemDirectoryHandle.testWriteAccess(): Boolean {
    return try {
        writeFile("TEST", "TEST")
        removeEntry("TEST").await()
        true
    } catch (_: Throwable) {
        false
    }
}

val isOpera = isOpera()

fun isOpera(): Boolean {
    val ua = js("navigator.userAgent") as String
    return ua.contains("OPR/") || ua.contains("Opera")
}