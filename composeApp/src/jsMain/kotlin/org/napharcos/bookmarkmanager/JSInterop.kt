package org.napharcos.bookmarkmanager

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

external interface FileSystemFileHandle {
    fun createWritable(): Promise<dynamic>
}

external interface FileSystemDirectoryHandle {
    fun getFileHandle(name: String, options: dynamic = definedExternally): Promise<FileSystemFileHandle>
}

external fun showDirectoryPicker(): Promise<FileSystemDirectoryHandle>