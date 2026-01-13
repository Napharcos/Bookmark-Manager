package org.napharcos.bookmarkmanager

import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.database.DatabaseRepository
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.time.ExperimentalTime

object BackupManager {
    private lateinit var database: DatabaseRepository
    private var isSaving = false
    const val CHANGES = "changes"
    private var changes = MutableStateFlow(window.localStorage[CHANGES]?.toInt() ?: 0)
    private var directory: FileSystemDirectoryHandle? = null
    private val channel = Channel<String>(Channel.UNLIMITED)
    val mutex = Mutex()

    @OptIn(ExperimentalTime::class)
    fun backupChanges() {
        AppScope.scope.launch {
            for (change in channel) {
                mutex.withLock {
                    directory?.writeFile(CHANGES, change, true)
                    changes.value += 1
                    window.localStorage[CHANGES] = changes.value.toString()
                }
            }
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    fun saveChanges() {
        AppScope.scope.launch {
            changes.collect {
                if (changes.value >= 100 && !isSaving) {
                    mutex.withLock {
                        isSaving = true
                        try {
                            val exportManager = ExportManager(database)
                            val text = exportManager.createBookmarksText(this)
                            val fileName = "Bookmarks - ${toDay()}"
                            directory?.writeFile(fileName, text, false)

                            changes.value = 0
                            window.localStorage[CHANGES] = changes.value.toString()

                            directory?.removeEntry(CHANGES)
                        } finally {
                            isSaving = false
                        }
                    }
                }
            }
        }
    }

    fun pushChanges(bookmark: Bookmarks) {
        val changesLogData = ChangesLogData(
            uuid = bookmark.uuid,
            parentId = bookmark.parentId,
            name = bookmark.name,
            modified = bookmark.modified,
            type = bookmark.type,
            url = bookmark.url,
            index = bookmark.index,
            imageId = bookmark.imageId,
            undoTrash = bookmark.undoTrash
        )

        val text = Json.encodeToString(changesLogData)
        val bytes = text.encodeToByteArray()

        channel.trySend("[${bytes.size}]$text")
    }

    fun initBackupFolder(database: DatabaseRepository, dir: FileSystemDirectoryHandle?) {
        AppScope.scope.launch {
            BackupManager.database = database
            directory = dir

            backupChanges()
            saveChanges()
        }
    }

    suspend fun restoreBackup(scope: CoroutineScope) {
        val files = directory?.listFiles() ?: emptyList()
        val bookmarkFiles = files.filter { it.name.startsWith("Bookmarks - ") }.sortedBy { it.name }
        if (bookmarkFiles.isNotEmpty()) {
            val lastFile = files.last().getFile().await()
            val text = (lastFile.asDynamic().text() as Promise<String>).await()
            val json = Json { ignoreUnknownKeys = true }
            var bookmarks = try { json.decodeFromString<BookmarkJson>(text) } catch (_: Throwable) { null }

            if (bookmarks == null || bookmarks.roots.bookmark_bar.children.isEmpty()) {
                val secondFile = files[files.size - 2].getFile().await()
                val text = (secondFile.asDynamic().text() as Promise<String>).await()
                bookmarks = try { json.decodeFromString<BookmarkJson>(text) } catch (_: Throwable) { null }
            }

            if (bookmarks != null) {
                val importManager = ImportManager(database)
                importManager.readBookmarksHelper(scope, bookmarks)
            }

            val changes = files.firstOrNull { it.name == CHANGES }

            if (changes != null) {
                files.readAndApplyChanges(changes)
            }
        }
    }

    suspend fun List<FileSystemFileHandle>.readAndApplyChanges(fileHandle: FileSystemFileHandle) {
        val file = fileHandle.getFile().await()

        val buffer = (file.asDynamic().arrayBuffer() as Promise<ArrayBuffer>).await()
        val bytes = Uint8Array(buffer)

        val changes = readChanges(bytes)

        changes.forEach { c ->
            val change = Json.decodeFromString<ChangesLogData>(c)

            database.addBookmark(change.toBookmarks())
        }

        val acceptedFormats = listOf("png", "jpg", "jpeg", "svg")
        val images = filter { acceptedFormats.contains(it.name.substringAfter('.')) }

        images.forEach { image ->
            AppScope.scope.launch {
                val id = image.name.substringBefore('.')
                val img = image.getFile().await().readImage()

                database.getBookmarkByImage(this, id)?.let {
                    database.updateImage(this@launch, it.uuid, img)
                }
            }
        }
    }

    suspend fun File?.readImage(): String {
        if (this == null) return ""

        val result = CompletableDeferred<String>()
        val reader = FileReader()

        reader.onload = {
            result.complete(reader.result as? String ?: "")
        }

        reader.onerror = {
            console.warn("Failed to read file: ${this.name}")
            result.complete("")
        }

        reader.readAsDataURL(this)
        return result.await()
    }

    fun readChanges(bytes: Uint8Array): List<String> {
        val result = mutableListOf<String>()
        var i = 0

        while (i < bytes.length) {
            if (bytes[i] != '['.code.toByte()) {
                error("Invalid format at $i")
            }
            i++

            val start = i
            while (bytes[i] != ']'.code.toByte()) i++

            val headerBytes = ByteArray(i - start) { bytes[start + it] }
            val len = headerBytes.decodeToString().toInt()
            i++

            val payloadBytes = ByteArray(len) { bytes[i + it] }
            result.add(payloadBytes.decodeToString())
            i += len
        }

        return result
    }

    suspend fun backupImage(image: String, imageId: String) = downloadImage(directory, image, imageId)

    fun changeBackupFolder(onComplete: () -> Unit) {
        AppScope.scope.launch {
            directory = showDirectoryPicker().await()

            if (directory != null && directory?.isWritable() == true) {
                if (!isOpera) database.saveBackupDir(directory!!)

                if (getBackupFiles().isEmpty()) {
                    createFirstBackup()
                } else if (database.getChilds(this, "").isEmpty()) {
                    restoreBackup(this)
                    onComplete()
                }
            }
        }
    }

    fun createFirstBackup() {
        AppScope.scope.launch {
            val exportManager = ExportManager(database)
            val text = exportManager.createBookmarksText(this)

            exportManager.exportVivaldiImages(directory)

            val fileName = "Bookmarks - ${toDay()}"
            try {
                directory?.writeFile(fileName, text, false)
            } catch (_: Throwable) {
                console.log("Failed to crete backup file: $fileName")
            }
        }
    }

    suspend fun getBackupFiles(): List<String> {
        val files = directory?.listFiles() ?: return emptyList()
        return files.filter { it.name.startsWith("Bookmarks - ") }.map { it.name }.sortedBy { it }
    }

    fun toDay(): String {
        val jsDate = Date()
        val year = jsDate.getFullYear()
        val month = (jsDate.getMonth() + 1).toString().padStart(2, '0')
        val day = jsDate.getDate().toString().padStart(2, '0')

        return "$year-$month-$day"
    }

    @Serializable
    data class ChangesLogData(
        val uuid: String,
        val parentId: String,
        val name: String,
        val modified: String,
        val type: String,
        val url: String,
        val index: Int,
        val imageId: String,
        val undoTrash: String
    ) {
        fun toBookmarks(): Bookmarks {
            return Bookmark(
                uuid = uuid,
                parentId = parentId,
                name = name,
                modified = modified,
                type = type,
                url = url,
                index = index,
                imageId = imageId,
                image = if (type == Constants.FOLDER) "./folder.svg" else "",
                undoTrash = undoTrash
            )
        }
    }
}