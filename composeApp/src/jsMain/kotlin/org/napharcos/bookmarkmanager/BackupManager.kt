package org.napharcos.bookmarkmanager

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.napharcos.bookmarkmanager.database.DatabaseRepository
import org.w3c.dom.set
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Date
import kotlin.time.ExperimentalTime

object BackupManager {
    private lateinit var database: DatabaseRepository

    private var isSaveing = false

    const val CHANGES = "changes"

    private var changes = MutableStateFlow(0)

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
                if (changes.value >= 500 && !isSaveing) {
                    mutex.withLock {
                        isSaveing = true
                        try {
                            val exportManager = ExportManager(database)
                            val text = exportManager.createBookmarksText(this)
                            val exportText: JsAny = text.toJsString()
                            val blob =
                                Blob(arrayOf(exportText).toJsArray(), BlobPropertyBag("application/octet-stream"))

                            val dir = directory ?: throw IllegalStateException("Directory not selected")
                            val fileName = "Bookmarks - ${toDay()}"

                            val fileHandle = dir.getFileHandle(fileName, js("{create: true}")).await()
                            val writable = fileHandle.createWritable().await()
                            writable.write(blob)
                            writable.close()

                            changes.value = 0
                            window.localStorage[CHANGES] = changes.value.toString()
                            directory?.getFileHandle(CHANGES)?.await()?.deleteFile()
                        } finally {
                            isSaveing = false
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

    suspend fun backupImage(image: String, imageId: String) = downloadImage(directory, image, imageId)

    fun changeBackupFolder(onComplete: () -> Unit) {
        AppScope.scope.launch {
            directory = showDirectoryPicker().await()

            if (directory != null && directory?.isValid() == true) {
                database.saveBackupDir(directory!!)
            }
        }
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
    )
}