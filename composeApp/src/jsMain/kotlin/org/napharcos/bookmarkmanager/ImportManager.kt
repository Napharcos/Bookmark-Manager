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
import kotlin.collections.component1
import kotlin.collections.component2

class ImportManager(
    private val browserDBRepository: DatabaseRepository,
    private val serverDBRepository: DatabaseRepository? = null
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

                isLoading = false
                loadingText = ""
            }
        }

        reader.readAsText(this)
    }

    private suspend fun List<BookmarkData>.addBookmarks(scope: CoroutineScope, parentId: String) {
        val browserRootChilds = browserDBRepository.getChilds(scope, parentId)
        val serverRootChilds = serverDBRepository?.getChilds(scope, parentId)

        var nextIndex = maxOf(
            browserRootChilds.maxByOrNull { it.index }?.index ?: 0,
            serverRootChilds?.maxByOrNull { it.index }?.index ?: 0
        )

        for (it in this) {
            val browserBookmark = browserDBRepository.getBookmark(scope, it.guid)
            val serverBookmark = serverDBRepository?.getBookmark(scope, it.guid)

            if (browserBookmark == null && serverBookmark == null) {
                loadingText = getString(Values.LOADING_ADD_BOOKMARK, it.name)

                val date = (if (it.date_modified.isNotEmpty() && it.date_modified != "0") it.date_modified else it.date_added).toLongOrNull()?.convertChromeTime() ?: 0L
                val imageId = if (it.meta_info?.imageID.isNullOrEmpty()) {
                    it.meta_info?.Thumbnail?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
                } else it.meta_info.imageID

                val newBookmark = Bookmark(
                    uuid = it.guid,
                    parentId = parentId,
                    name = it.name,
                    modified = date.toString(),
                    type = if (it.type == Constants.FOLDER) Constants.FOLDER else Constants.URL,
                    url = it.url,
                    index = nextIndex,
                    imageId = imageId,
                    image = if (it.type == Constants.FOLDER) "./folder.svg" else "",
                    undoTrash = it.meta_info?.undoTrashParentId ?: ""
                )

                browserDBRepository.addBookmark(newBookmark)
                serverDBRepository?.addBookmark(newBookmark)

                nextIndex++
            } else if (browserBookmark == null || (serverDBRepository != null && serverBookmark == null)) {
                // TODO: sync
            }

            it.children.addBookmarks(scope, it.guid)
        }
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
            serverDBRepository?.updateImage(scope, bookmark.uuid, "data:image/jpeg;base64,$image")
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
                            // Kulcs–érték kész, feldolgozzuk
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
                    serverDBRepository?.updateImage(this, bookmark.uuid, base64)
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
}