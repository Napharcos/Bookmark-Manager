package org.napharcos.bookmarkmanager

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.js.Promise

sealed class FileSystemOperation {
    data class WriteTextFile(val name: String, val content: String, val keep: Boolean = false) : FileSystemOperation()
    data class WriteImageFile(val name: String, val content: org.w3c.files.Blob) : FileSystemOperation()
    data class DeleteFile(val name: String) : FileSystemOperation()
    data class VerifyAccess(val name: String = "TEST") : FileSystemOperation()
}

class FileSystemWriteAPI(private val directory: FileSystemDirectoryHandle?) {
    private val channel = Channel<FileSystemOperation>(Channel.UNLIMITED)

    private var job: Job? = null

    var accessGranted = CompletableDeferred<Boolean>()
        private set

    init {
        startWriter()
    }

    fun startWriter() {
        job = AppScope.scope.launch {
            for (op in channel) {
                when (op) {
                    is FileSystemOperation.WriteTextFile -> {
                        try {
                            directory?.writeFile(op.name, op.content, op.keep)
                        } catch (e: Throwable) {
                            console.error("Failed to write file ${op.name}", e)
                        }
                    }
                    is FileSystemOperation.WriteImageFile -> {
                        try {
                            directory?.writeFile(op.name, op.content)
                        } catch (e: Throwable) {
                            console.error("Failed to write image ${op.name}", e)
                        }
                    }
                    is FileSystemOperation.DeleteFile -> {
                        try {
                            directory?.removeEntry(op.name)
                        } catch (e: Throwable) {
                            console.error("Failed to remove file ${op.name}", e)
                        }
                    }
                    is FileSystemOperation.VerifyAccess -> {
                        try {
                            directory?.writeFile(op.name, "TEST")
                            directory?.removeEntry(op.name)
                            accessGranted.complete(true)
                        } catch (e: Throwable) {
                            console.error("Failed to write file ${op.name}", e)
                            accessGranted.complete(false)
                        }
                    }
                }
            }
        }
    }

    suspend fun addOperation(f: FileSystemOperation) {
        try { channel.send(f) } catch (e: Throwable) { console.error(e) }
    }

    private suspend fun FileSystemDirectoryHandle.writeFile(
        name: String,
        content: Any,
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

    suspend fun verifyDir(): Boolean {
        accessGranted = CompletableDeferred()
        addOperation(FileSystemOperation.VerifyAccess())
        return accessGranted.await()
    }

    suspend fun shutDown() {
        channel.close()
        job?.join()
    }
}