package org.napharcos.bookmarkmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.database.DatabaseRepository
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ImportManager(
    private val browserDBRepository: DatabaseRepository
) {
    companion object {
        const val BOOKMARK_FILE_PATHS= """
            |Brave: c:\Users\username\AppData\Local\BraveSoftware\Brave-Browser\User Data\Default\Bookmarks
            |Chrome: c:\Users\username\AppData\Local\Google\Chrome\User Data\Default\Bookmarks 
            |Edge: c:\Users\username\AppData\Local\Microsoft\Edge\User Data\Default\Bookmarks 
            |Opera: c:\Users\username\AppData\Roaming\Opera Software\Opera Stable\Default\Bookmarks 
            |Vivaldi: c:\Users\username\AppData\Local\Vivaldi\User Data\Default\Bookmarks
        """
        const val OPERA_IMAGE_FILE_PATH = """
            |Opera: c:\Users\Napharcos\AppData\Roaming\Opera Software\Opera Stable\Default\BookmarksExtras
        """
        const val VIVALDI_IMAGE_FOLDER_PATH = """
            |Vivaldi: c:\Users\Napharcos\AppData\Local\Vivaldi\User Data\Default\VivaldiThumbnails\
        """

        var isLoading by mutableStateOf(false)
        var loadingText by mutableStateOf("")

        var duplicateUuid by mutableStateOf<BookmarkData?>(null)
        var duplicateUrl by mutableStateOf<BookmarkData?>(null)
        var newParentId by mutableStateOf("")

        var manageDuplicateUuid: Int? = null
        var manageDuplicateUrl: Int? = null

        var duplicate: CompletableDeferred<Int>? = null
    }

    fun importBookmarksFile() {
        val input = document.createElement("input") as? HTMLInputElement ?: return
        input.type = "file"

        input.onchange = { _ ->
            input.files?.get(0)?.let {
                if (it.size.toDouble() > 50 * 1024 * 1024)
                   importLargeJsonFile(it)
                else it.readBookmarksJson()
            }
        }

        input.click()
    }

    fun importOperaImages() {
        val input = document.createElement("input") as? HTMLInputElement ?: return
        input.type = "file"

        input.onchange = { _ ->
            input.files?.get(0)?.readJsonImage()
        }

        input.click()
    }

    fun importVivaldiImages() {
        val input = document.createElement("input") as? HTMLInputElement ?: return
        input.type = "file"
        input.asDynamic().webkitdirectory = true

        input.onchange = { _ ->
            isLoading = true
            AppScope.scope.launch {
                loadingText = getString(Values.LOADING_FILE)
                val files = input.files

                if (files != null)
                    (0 until files.length)
                        .mapNotNull { files[it] }
                        .filter { it.type.startsWith("image/") }
                        .forEach { it.readImage() }
                isLoading = false
                loadingText = ""
            }
        }

        input.click()
    }

    private fun File.readBookmarksJson() {
        val reader = FileReader()

        reader.onload = {
            isLoading = true
            AppScope.scope.launch {
                loadingText = getString(Values.LOADING_FILE)
                val data = reader.result as? String ?: ""
                val json = Json { ignoreUnknownKeys = true }
                val bookmarks = json.decodeFromString<BookmarkJson>(data)
                val rootFolders = mutableListOf<BookmarkData>()

                bookmarks.roots.bookmark_bar.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.other.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.synced.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.custom_root?.userRoot?.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.custom_root?.speedDial?.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.custom_root?.unsorted?.let { if (it.children.isNotEmpty()) rootFolders.add(it) }
                bookmarks.roots.custom_root?.unsyncedPinboard?.let { if (it.children.isNotEmpty()) rootFolders.add(it) }

                rootFolders.addBookmarks(this, "")

                bookmarks.roots.trash?.children?.addBookmarks(this, Constants.TRASH)
                bookmarks.roots.custom_root?.trash?.children?.addBookmarks(this, Constants.TRASH)

                manageDuplicateUrl = null
                manageDuplicateUuid = null
                isLoading = false
                loadingText = ""
            }
        }

        reader.readAsText(this)
    }

    private suspend fun List<BookmarkData>.addBookmarks(scope: CoroutineScope, parentId: String) {
        val browserRootChilds = browserDBRepository.getChilds(scope, parentId)

        var nextIndex = browserRootChilds.maxByOrNull { it.index }?.index ?: 0

        for (it in this) {
            loadingText = getString(Values.LOADING_ADD_BOOKMARK, it.name)

            val uuidBookmark = browserDBRepository.getBookmark(scope, it.guid)
            val urlBookmark = if (it.url.isNotEmpty()) browserDBRepository.getBookmarkByUrl(scope, it.url) else null

            if (uuidBookmark == null && urlBookmark == null) {
                nextIndex = addBookmark(it, parentId, nextIndex)
            } else if (uuidBookmark?.uuid == it.guid) {
                val override = override(Type.UUID, parentId, it)

                if (override == 1)
                    nextIndex = addBookmark(it, parentId, nextIndex, true)
                else if (override == 2)
                    nextIndex = addBookmark(it, parentId, nextIndex, newUUID = true)
            } else if (urlBookmark?.url == it.url) {
                console.log("old: ${urlBookmark.url}")
                console.log("new: ${it.url}")

                val override = override(Type.URL, parentId, it)

                if (override == 1)
                    nextIndex = addBookmark(it, parentId, nextIndex)
            }

            it.children.addBookmarks(scope, it.guid)
        }
    }

    private suspend fun override(type: Type, parentId: String, element: BookmarkData): Int? {
        var override = when (type) {
            Type.UUID -> manageDuplicateUuid
            Type.URL -> manageDuplicateUrl
        }

        if (override == null) {
            duplicate = CompletableDeferred()
            newParentId = parentId
            if (type == Type.UUID) duplicateUuid = element
            if (type == Type.URL) duplicateUrl = element
            override = duplicate?.await()
            duplicate = null
            newParentId = ""
            duplicateUuid = null
            duplicateUrl = null
        }

        return override
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun addBookmark(element: BookmarkData, parentId: String, nextIndex: Int, override: Boolean = false, newUUID: Boolean = false): Int {
        val date =
            (if (element.date_modified.isNotEmpty() && element.date_modified != "0") element.date_modified else element.date_added).toLongOrNull()
                ?.convertChromeTime() ?: 0L
        val imageId = if (element.meta_info?.imageID.isNullOrEmpty()) {
            element.meta_info?.Thumbnail?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
        } else element.meta_info.imageID

        val newBookmark = Bookmark(
            uuid = if (newUUID) Uuid.random().toHexString() else element.guid,
            parentId = parentId,
            name = element.name,
            modified = date.toString(),
            type = if (element.type == Constants.FOLDER) Constants.FOLDER else Constants.URL,
            url = element.url,
            index = nextIndex,
            imageId = imageId,
            image = if (element.type == Constants.FOLDER) "./folder.svg" else "",
            undoTrash = element.meta_info?.undoTrashParentId ?: ""
        )

        browserDBRepository.addBookmark(newBookmark, override)

        return nextIndex + 1
    }

    private fun File.readJsonImage() {
        val reader = FileReader()

        reader.onload = {
            isLoading = true
            AppScope.scope.launch {
                loadingText = getString(Values.LOADING_FILE)
                val data = reader.result as? String ?: ""
                val images = Json.decodeFromString<Map<String, String>>(data)

                images.forEach { (imageId, image) ->
                    saveData(this, imageId, image)
                }
                isLoading = false
                loadingText = ""
            }
        }

        reader.onerror = {
            console.warn("Failed to read file: ${this.name}")
        }

        reader.readAsText(this)
    }

    suspend fun saveData(scope: CoroutineScope, imageId: String, image: String) {
        val bookmark = browserDBRepository.getBookmarkByImage(scope, imageId)

        bookmark?.let {
            loadingText = getString(Values.LOADING_UPDATE_BOOKMARK, bookmark.name)

            browserDBRepository.updateImage(scope, bookmark.uuid, "data:image/jpeg;base64,$image")
        }
    }

    fun importLargeJsonFile(file: File) {
        AppScope.scope.launch {
            isLoading = true
            loadingText = getString(Values.LOADING_FILE)
            val reader = (file as Blob).stream().getReader()
            val decoder = TextDecoder("utf-8")

            var insideKey = false
            var insideValue = false
            var key = ""
            var value = ""
            var isEscaped = false

            suspend fun processBufferChar(c: Char) {
                when {
                    insideKey -> {
                        if (isEscaped) {
                            key += c
                            isEscaped = false
                        } else if (c == '\\') {
                            isEscaped = true
                        } else if (c == '"') {
                            insideKey = false
                        } else {
                            key += c
                        }
                    }

                    insideValue -> {
                        if (isEscaped) {
                            value += c
                            isEscaped = false
                        } else if (c == '\\') {
                            isEscaped = true
                        } else if (c == '"') {
                            insideValue = false
                            saveData(this, key, value)
                            key = ""
                            value = ""
                        } else {
                            value += c
                        }
                    }

                    else -> {
                        if (c == '"') {
                            if (key.isEmpty()) {
                                insideKey = true
                            } else if (!insideValue) {
                                insideValue = true
                            }
                        }
                    }
                }
            }

            while (true) {
                val chunkResult = reader.read().await()
                if (chunkResult.done) break

                val chunkText = decoder.decode(chunkResult.value)
                for (c in chunkText) {
                    processBufferChar(c)
                }
            }
            isLoading = false
            loadingText = ""
        }
    }

    private suspend fun File.readImage() {
        val result = CompletableDeferred<Unit>()
        val reader = FileReader()

        reader.onload = {
            AppScope.scope.launch {
                val imageId = this@readImage.name.substringBeforeLast('.')
                val bookmark = browserDBRepository.getBookmarkByImage(this, imageId)

                bookmark?.imageId?.let {
                    loadingText = getString(Values.LOADING_UPDATE_BOOKMARK, bookmark.name)
                    val base64 = reader.result as? String ?: ""

                    browserDBRepository.updateImage(this, bookmark.uuid, base64)
                }
                result.complete(Unit)
            }
        }

        reader.onerror = {
            console.warn("Failed to read file: ${this.name}")
        }

        reader.readAsDataURL(this)
        result.await()
    }

    enum class Type { UUID, URL }
}