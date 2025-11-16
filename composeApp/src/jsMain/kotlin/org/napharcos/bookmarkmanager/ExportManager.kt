package org.napharcos.bookmarkmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.database.DatabaseRepository
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExportManager(private val database: DatabaseRepository) {

    private var nextId = 0

    private val idsMap = mutableMapOf<String, Int>()

    private var directory: FileSystemDirectoryHandle? = null

    fun exportVivaldiImages() {
        AppScope.scope.launch {
            directory = showDirectoryPicker().await()

            isLoading = true
            loadingText = ""

            manageImages(this, "")
            manageImages(this, Constants.TRASH)

            directory = null
            loadingText = ""
            isLoading = false
        }
    }

    suspend fun downloadImage(image: String, imageId: String) {
        val dir = directory ?: throw IllegalStateException("Directory not selected")
        val response = window.fetch(image).await()
        val blob = response.blob().await()

        val ext = when (blob.type) {
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            "image/svg+xml" -> "svg"
            ".png" -> "png"
            ".jpg" -> "jpg"
            ".jpeg" -> "jpg"
            ".svg" -> "svg"
            else -> "bin"
        }

        val fileName = "$imageId.$ext"

        val fileHandle = dir.getFileHandle(fileName, js("{create: true}")).await()
        val writable = fileHandle.createWritable().await()
        writable.write(blob)
        writable.close()
    }

    private suspend fun manageImages(scope: CoroutineScope, parent: String) {
        val childs = database.getChilds(scope, parent)

        childs.forEach {
            if (it.type == Constants.FOLDER)
                manageImages(scope, it.uuid)

            loadingText = getString(Values.PROCESSING_ELEMENT, it.name)

            if (it.imageId.isNotEmpty() && it.image.isNotEmpty())
                downloadImage(it.image, it.imageId)
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    fun exportBookmarkFile() {
        AppScope.scope.launch {
            isLoading = true
            nextId = 0
            idsMap.clear()
            val text = createBookmarksText(this)
            loadingText = getString(Values.CREATING_FILE)
            val content: JsAny = text.toJsString()
            val blob = Blob(arrayOf(content).toJsArray(), BlobPropertyBag(type = "application/octet-stream"))
            val url = URL.createObjectURL(blob)
            val link = document.createElement("a") as HTMLAnchorElement
            link.href = url
            link.download = "Bookmarks"
            document.body?.appendChild(link)
            link.click()
            document.body?.removeChild(link)
            URL.revokeObjectURL(url)
            idsMap.clear()
            nextId = 0
            isLoading = false
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private suspend fun createBookmarksText(scope: CoroutineScope): String {
        val bookmarkBar = buildBookmarkTree(scope, "")
        val trashContent = buildBookmarkTree(scope, Constants.TRASH)

        loadingText = getString(Values.CREATING_STRUCTURE)

        val roots = BookmarkJsonRoots(
            bookmark_bar = bookmarkBar,
            other = BookmarkData(
                date_added = Clock.System.now().toEpochMilliseconds().toChromeTime().toString(),
                date_modified = Clock.System.now().toEpochMilliseconds().toChromeTime().toString(),
                guid = Uuid.random().toHexString(),
                id = (nextId + 1).toString(),
                name = "Other",
                type = Constants.FOLDER
            ),
            synced = BookmarkData(
                date_added = Clock.System.now().toEpochMilliseconds().toChromeTime().toString(),
                date_modified = Clock.System.now().toEpochMilliseconds().toChromeTime().toString(),
                guid = Uuid.random().toHexString(),
                id = (nextId + 2).toString(),
                name = "Synced",
                type = Constants.FOLDER
            ),
            trash = trashContent
        )

        val jsonData = BookmarkJson(
            checksum = "",
            roots = roots,
            version = 1
        )

        return Json.encodeToString(jsonData)
    }

    private suspend fun buildBookmarkTree(scope: CoroutineScope, rootId: String): BookmarkData {
        val rootData = database.getBookmark(scope, rootId)
        val children = database.getChilds(scope, rootId).sortedBy { it.index }

        loadingText = getString(Values.PROCESSING_ELEMENT, rootData?.name ?: "")

        val modified = rootData?.modified?.toLong()?.toChromeTime()?.toString() ?: ""

        val childNodes = children.map { buildBookmarkNode(scope, it) }

        nextId++
        idsMap[rootId] = nextId

        return BookmarkData(
            children = childNodes,
            date_added = modified,
            date_modified = modified,
            guid = rootId.ifEmpty { Constants.ROOT_ID },
            id = nextId.toString(),
            meta_info = BookmarkMeta(
                imageID = rootData?.imageId ?: "",
                Thumbnail = rootData?.imageId ?: ""
            ),
            name = rootData?.name ?: "",
            type = Constants.FOLDER,
            url = ""
        )
    }

    private suspend fun buildBookmarkNode(scope: CoroutineScope, bookmark: Bookmarks): BookmarkData {
        val modified = bookmark.modified.toLong().toChromeTime().toString()

        val undoTrashId = idsMap[bookmark.undoTrash]

        loadingText = getString(Values.PROCESSING_ELEMENT, bookmark.name)

        return if (bookmark.type == Constants.FOLDER) {
            val children = database.getChilds(scope, bookmark.uuid).sortedBy { it.index }
            val childNodes = children.map { child ->
                buildBookmarkNode(scope, child)
            }

            nextId++
            idsMap[bookmark.uuid] = nextId

            BookmarkData(
                children = childNodes,
                date_added = modified,
                date_modified = modified,
                guid = bookmark.uuid,
                id = nextId.toString(),
                meta_info = BookmarkMeta(
                    imageID = bookmark.imageId,
                    Thumbnail = bookmark.imageId,
                    undoTrashParentId = undoTrashId?.toString() ?: ""
                ),
                name = bookmark.name,
                type = Constants.FOLDER,
                url = ""
            )
        } else {
            nextId++
            idsMap[bookmark.uuid] = nextId

            BookmarkData(
                children = emptyList(),
                date_added = modified,
                date_modified = modified,
                guid = bookmark.uuid,
                id = nextId.toString(),
                meta_info = BookmarkMeta(
                    imageID = bookmark.imageId,
                    Thumbnail = bookmark.imageId,
                    undoTrashParentId = undoTrashId?.toString() ?: ""
                ),
                name = bookmark.name,
                type = Constants.URL,
                url = bookmark.url
            )
        }
    }

    companion object {
        var isLoading by mutableStateOf(false)

        var loadingText by mutableStateOf("")
    }
}