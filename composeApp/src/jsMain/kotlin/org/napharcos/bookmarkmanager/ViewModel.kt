package org.napharcos.bookmarkmanager

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.napharcos.bookmarkmanager.container.Container
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.popup.PageInfo
import org.napharcos.bookmarkmanager.popup.onPopupOpen
import org.napharcos.bookmarkmanager.popup.toPageData
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModel(private val container: Container, private val popup: Boolean) {

    val importManager = ImportManager(container.browserDatabase)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        initUIState()
    }

    fun initUIState() {
        if (!popup) {
            _uiState.update {
                it.copy(
                    background = window.localStorage[Constants.BACKGROUND] ?: "",
                    darkening = window.localStorage[Constants.DARKENING] != Constants.FALSE,
                    textColor = window.localStorage[Constants.TEXT_COLOR]?.toInt() ?: it.textColor,
                    cardSize = window.localStorage[Constants.CARD_SIZE]?.toInt() ?: it.cardSize
                )
            }

        }

        AppScope.scope.launch {
            var showDialog = false
            val backupDir = if (isOpera) null else container.browserDatabase.getBackupDir(this)

            val api = FileSystemWriteAPI(backupDir)

            if (!popup)
                showDialog = try {
                    backupDir == null || !backupDir.isWritable() || !api.verifyDir()
                } catch (_: Throwable) { true }

            if (showDialog)
                _uiState.update { it.copy(showingAddBackupFolderDialog = true) }

            BackupManager.initBackupFolder(container.browserDatabase, backupDir)
        }

        AppScope.scope.launch {
            val children = container.browserDatabase.getSpecificFolders(this, "")
            val newFolders = mutableListOf<Bookmarks>()

            children.forEach {
                newFolders.add(it)
                newFolders.addAll(container.browserDatabase.getSpecificFolders(this, it.uuid))
            }

            val lastOpenFolder = if (popup) "" else window.localStorage[Constants.LAST_OPEN_FOLDER] ?: ""

            val folders = getFolders(this, lastOpenFolder)

            val openFolders = openParents(this, lastOpenFolder).distinct()

            _uiState.update {
                it.copy(
                    openFolders = openFolders,
                    selectedFolder = lastOpenFolder,
                    folders = if (lastOpenFolder == "") newFolders else (newFolders + folders).distinctBy { f -> f.uuid },
                    folderContent = if (popup)
                        emptyList()
                    else container.browserDatabase.getChilds(this, lastOpenFolder).createImageIfNotExist()
                        .sortedBy { c -> c.index },
                    selectedElements = emptyList()
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createNewPopupFolder(parent: String) {
        AppScope.scope.launch {
            val uuid = Uuid.random().toHexString()

            val newBookmark = Bookmark(
                uuid = uuid,
                parentId = parent,
                name = getString(Values.NEW_FOLDER),
                type = Constants.FOLDER,
                index = getNextIndex(this, parent),
                imageId = "",
                image = "./folder.svg"
            )

            container.browserDatabase.addBookmark(this, newBookmark)
            BackupManager.pushChanges(newBookmark)

            if (!uiState.value.openFolders.contains(parent))
                onNavElementFoldClick(parent)

            _uiState.update {
                it.copy(
                    folders = (it.folders + newBookmark).distinctBy { f -> f.uuid },
                    selectedFolder = uuid,
                    newFolder = uuid
                )
            }
        }
    }

    fun onPopupEditConfirm(bookmark: Bookmarks, name: String) {
        AppScope.scope.launch {
            val updatedBookmark = bookmark.copy(name = name)

            container.browserDatabase.addBookmark(this, updatedBookmark, true)
            BackupManager.pushChanges(updatedBookmark)

            _uiState.update {
                it.copy(
                    folders = (listOf(updatedBookmark) + it.folders).distinctBy { f -> f.uuid },
                    selectedFolder = bookmark.uuid,
                    newFolder = null
                )
            }
        }
    }

    fun onPopupTrashClick() {
        AppScope.scope.launch {
            val pageData = uiState.value.pageData
            val bookmarkData = pageData?.url?.let { container.browserDatabase.getBookmarkByUrl(this, it) }

            bookmarkData?.let {
                container.browserDatabase.deleteBookmark(this, it.uuid)
                BackupManager.pushChanges(it.copy(parentId = Constants.DELETED))
                BackupManager.deleteImage(bookmarkData.image, bookmarkData.imageId)
            }

            chrome.tabs.query(js("{active: true, currentWindow: true}")) { tabs ->
                val tab = tabs[0] ?: return@query
                val tabId = tab.id as Int

                updateIcon(tabId, false)
            }
            BackupManager.shutDown()
            window.close()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onPopupOkClick(image: String) {
        AppScope.scope.launch {
            val parent = uiState.value.selectedFolder
            val pageData = uiState.value.pageData
            val bookmarkData = pageData?.url?.let { container.browserDatabase.getBookmarkByUrl(this, it) }

            if (image != bookmarkData?.image || parent != bookmarkData.parentId) {
                val bookmark = bookmarkData?.copy(
                    parentId = parent,
                    imageId = if (image != bookmarkData.image) Uuid.random().toHexString() else bookmarkData.imageId,
                    image = image
                ) ?: Bookmark(
                    parentId =  parent,
                    name = pageData?.title ?: "",
                    type = Constants.URL,
                    index = getNextIndex(this, parent),
                    imageId = Uuid.random().toHexString(),
                    image = image,
                    url = pageData?.url ?: "",
                )

                container.browserDatabase.addBookmark(this, bookmark, true)
                BackupManager.pushChanges(bookmark)
                if (image != bookmarkData?.image) {
                    bookmarkData?.let {
                        BackupManager.deleteImage(it.image, it.imageId)
                    }
                    BackupManager.backupImage(image, bookmark.imageId)
                }

                window.localStorage[Constants.LAST_FOLDER] = parent
            }
            chrome.tabs.query(js("{active: true, currentWindow: true}")) { tabs ->
                val tab = tabs[0] ?: return@query
                val tabId = tab.id as Int

                updateIcon(tabId, true)
            }
            BackupManager.shutDown()
            window.close()
        }
    }

    fun updateIcon(tabId: Int, isBookmarked: Boolean) {
        val iconPaths = js("{}")
        iconPaths["16"] = if (isBookmarked) "icons/bookmark_16.png" else "icons/bookmark_border_16.png"
        iconPaths["32"] = if (isBookmarked) "icons/bookmark_32.png" else "icons/bookmark_border_32.png"
        iconPaths["48"] = if (isBookmarked) "icons/bookmark_48.png" else "icons/bookmark_border_48.png"
        iconPaths["128"] = if (isBookmarked) "icons/bookmark_128.png" else "icons/bookmark_border_128.png"

        val details = js("{}")
        details["path"] = iconPaths
        details["tabId"] = tabId

        chrome.action.setIcon(details)
    }

    fun getPageData() {
        val options = js("{}").unsafeCast<CaptureOptions>()
        options.format = "png"

        chrome.tabs.captureVisibleTab(null, options) { screenShot ->
            onPopupOpen { msg ->
                val message = msg.unsafeCast<dynamic>()

                if (message.type == "PAGE_INFO") {
                    val payload = message.payload.unsafeCast<dynamic>()
                    val url = payload.fullUrl as? String ?: ""

                    AppScope.scope.launch {
                        val bookmark = container.browserDatabase.getBookmarkByUrl(this, url)

                        val info = PageInfo(
                            fullUrl = url,
                            baseDomain = payload.baseDomain as? String ?: "",
                            title = payload.title as? String ?: "",
                            favicons = (payload.favicons as? Array<dynamic>)?.mapNotNull { it as? String }
                                ?.toTypedArray()
                                ?: emptyArray(),
                            metaImages = (payload.metaImages as? Array<dynamic>)?.mapNotNull { it as? String }
                                ?.toTypedArray() ?: emptyArray(),
                            pageImages = (payload.pageImages as? Array<dynamic>)?.mapNotNull { it as? String }
                                ?.toTypedArray() ?: emptyArray()
                        )

                        val parentId = bookmark?.parentId ?: window.localStorage[Constants.LAST_FOLDER]

                        val folders = getFolders(this, parentId ?: "")

                        val openFolders = (listOf(parentId) + (parentId?.let { openParents(this, it) } ?: emptyList())).mapNotNull { it }

                        _uiState.update {
                            it.copy(
                                pageData = info.toPageData(
                                    screenShot = screenShot,
                                    recoveredImage = bookmark?.image ?: "",
                                    title = bookmark?.name ?: ""
                                ),
                                selectedFolder = parentId ?: "",
                                folders = (it.folders + folders).distinctBy { f -> f.uuid },
                                openFolders = (it.openFolders + openFolders).distinct()
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun getFolders(scope: CoroutineScope, parentId: String): List<Bookmarks> {
        val result = mutableListOf<Bookmarks>()

        var currentId: String? = parentId

        while (!currentId.isNullOrEmpty()) {
            val parent = container.browserDatabase.getBookmark(scope, currentId)
            if (parent != null) {
                result.add(parent)

                val children = container.browserDatabase.getSpecificFolders(scope, parent.uuid)
                result.addAll(children)

                for (child in children) {
                    val childs = container.browserDatabase.getSpecificFolders(scope, child.uuid)

                    result.addAll(childs)
                }

                currentId = parent.parentId
            } else {
                currentId = null
            }
        }

        return result.distinctBy { it.uuid }
    }

    suspend fun openParents(scope: CoroutineScope, parentId: String): List<String> {
        val result = mutableListOf<String>()
        var currentId: String? = parentId

        while (!currentId.isNullOrEmpty()) {
            val bookmark = container.browserDatabase.getBookmark(scope, currentId) ?: break
            result.add(currentId)
            currentId = bookmark.parentId
        }

        return result
    }


    fun updateShowingClearTrash(showing: Boolean) {
        _uiState.update {
            it.copy(showingClearTrashDialog = showing)
        }
    }

    fun clearTrashElements() {
        _uiState.update {
            it.copy(trashElements = emptyList())
        }
    }

    fun updateDeleteElements(list: List<String>) {
        _uiState.update {
            it.copy(deleteElements = list)
        }
    }

    fun updateShowingChangeBackground(showing: Boolean) {
        _uiState.update {
            it.copy(showingChangeBackgroundDialog = showing)
        }
    }

    fun updateShowingNewElement(showing: Boolean) {
        _uiState.update {
            it.copy(showingAddNewElementDialog = showing)
        }
    }

    fun updateEditElement(element: Bookmarks?) {
        _uiState.update {
            it.copy(editElement = element)
        }
    }

    fun updateShowingImportDialog(showing: Boolean) {
        _uiState.update {
            it.copy(showingImportBookmarksDialog = showing)
        }
    }

    fun updateShowingExportDialog(showing: Boolean) {
        _uiState.update {
            it.copy(showingExportBookmarksDialog = showing)
        }
    }

    fun updateShowingDeleteDBDialog(showing: Boolean) {
        _uiState.update {
            it.copy(showingDeleteDBDialog = showing)
        }
    }

    fun onSelectElementClick(select: Boolean, bookmark: String) {
        _uiState.update {
            it.copy(
                selectedElements = if (select) (it.selectedElements + bookmark).distinct() else it.selectedElements - bookmark
            )
        }
    }

    fun onBackgroundConfirmClick(image: String) {
        _uiState.update {
            it.copy(
                background = image,
                showingChangeBackgroundDialog = false
            )
        }
        window.localStorage[Constants.BACKGROUND] = image
    }

    fun onAddBackupFolderConfirmClick() {
        BackupManager.changeBackupFolder { reloadData() }
        _uiState.update {
            it.copy(showingAddBackupFolderDialog = false)
        }
    }

    fun updateShowingTerms(showing: Boolean) {
        _uiState.update {
            it.copy(showingTermsDialog = showing)
        }
    }

    fun updateShowingLibraries(showing: Boolean) {
        _uiState.update {
            it.copy(showingLibrariesDialog = showing)
        }
    }

    fun clearTrash() {
        AppScope.scope.launch {
            val browserTrashElements = container.browserDatabase.getChilds(this, Constants.TRASH)

            val allElements = browserTrashElements.map { it.uuid }.distinct()

            allElements.forEach {
                deleteElementsRecursive(this, it, mutableSetOf())
            }
            updateShowingClearTrash(false)
            reloadData()
        }
    }

    fun deleteElements() {
        val elements = uiState.value.deleteElements

        AppScope.scope.launch {
            elements.forEach {
                deleteElementsRecursive(this, it, mutableSetOf())
            }
            updateDeleteElements(emptyList())
            reloadData()
        }
    }

    suspend fun deleteElementsRecursive(scope: CoroutineScope, element: String, visited: MutableSet<String>) {
        if (!visited.add(element)) return

        val browserChilds = container.browserDatabase.getChilds(scope, element)
        val elementData = container.browserDatabase.getBookmark(scope, element)

        container.browserDatabase.deleteBookmark(scope, element)
        elementData?.let {
            BackupManager.pushChanges(it.copy(parentId = Constants.DELETED))
            BackupManager.deleteImage(it.image, it.imageId)
        }

        val allChilds = browserChilds.map { it.uuid }.distinct()

        allChilds.forEach { child ->
            deleteElementsRecursive(scope, child, visited)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddNewElementConfirmClick(type: String, name: String, url: String, image: String) {
        AppScope.scope.launch {
            val parent = uiState.value.selectedFolder
            val imageId = if (image.isNotEmpty()) Uuid.random().toHexString() else ""

            val newBookmark = Bookmark(
                parentId = parent,
                name = name.ifEmpty { url.substringAfter("//").substringBefore("/") },
                type = type,
                url = url,
                index = getNextIndex(this, parent),
                imageId = imageId,
                image = if (image.isEmpty() && type == Constants.FOLDER) "./folder.svg" else image
            )

            container.browserDatabase.addBookmark(this, newBookmark)
            BackupManager.pushChanges(newBookmark)
            if (image.isNotEmpty())
                BackupManager.backupImage(image, imageId)

            reloadData()
            updateShowingNewElement(false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onEditConfirmClick(bookmark: Bookmarks, name: String, url: String, image: String) {
        AppScope.scope.launch {
            val imageId = if (image != bookmark.image) Uuid.random().toHexString() else bookmark.imageId

            val newBookmark = bookmark.copy(
                name = name.ifEmpty { bookmark.name },
                url = url.ifEmpty { bookmark.url },
                imageId = imageId,
                image = image
            )

            container.browserDatabase.addBookmark(this, newBookmark, true)
            BackupManager.pushChanges(newBookmark)
            if (image != bookmark.image) {
                BackupManager.deleteImage(bookmark.image, bookmark.imageId)
                BackupManager.backupImage(image, imageId)
            }

            reloadData()
            updateEditElement(null)
        }
    }

    suspend fun getNextIndex(scope: CoroutineScope, parentId: String): Int {
        val browserChilds = container.browserDatabase.getChilds(scope, parentId)

        return (browserChilds.maxByOrNull { it.index }?.index ?: 0) + 1
    }

    fun reindexElements(element: String, target: String, before: Boolean) {
        val selected = (uiState.value.selectedElements + element).distinct()
        val list = uiState.value.folderContent.sortedBy { it.index }
        val targetData = list.first { it.uuid == target }
        val targetIndex = targetData.index

        val staticPart = if (before)
            list.filter { it.index < targetIndex && it.uuid !in selected }
        else
            list.filter { it.index <= targetIndex && it.uuid !in selected }

        val movingPart = list.filter { it.uuid in selected }

        val trailingPart = if (before)
            list.filter { it.index >= targetIndex && it.uuid !in selected }
        else list.filter { it.index > targetIndex && it.uuid !in selected }

        val newOrder = staticPart + movingPart + trailingPart

        AppScope.scope.launch {
            newOrder.forEachIndexed { i, item ->
                val newBookmark = item.copy(index = i)
                container.browserDatabase.addBookmark(this, newBookmark, true)
                BackupManager.pushChanges(newBookmark)
            }
            reloadData()
        }
    }

    fun moveElementToFolder(element: String, target: String, confirmed: Boolean = false, onlyOne: Boolean = false) {
        val elements = if (!onlyOne) (uiState.value.selectedElements + element).distinctBy { it } else listOf(element)
        val elementsData = uiState.value.folderContent.filter { it.uuid in elements }

        AppScope.scope.launch {
            if (target == Constants.TRASH && !confirmed) {
                _uiState.update { it.copy(trashElements = elementsData.map { e -> e.uuid }) }
                return@launch
            }

            var nextIndex = getNextIndex(this, target)

            elementsData.forEach {
                val newBookmark = when (target) {
                    Constants.TRASH -> it.copy(parentId = target, undoTrash = it.parentId, index = nextIndex)
                    it.undoTrash -> it.copy(parentId = it.undoTrash, undoTrash = "", index = nextIndex)
                    else -> it.copy(parentId = target, index = nextIndex)
                }

                container.browserDatabase.addBookmark(this, newBookmark, override = true)
                BackupManager.pushChanges(newBookmark)

                nextIndex++
            }

            _uiState.update { it.copy(trashElements = emptyList()) }
            reloadData()
        }
    }

    suspend fun onBrowseImageClick(): String {
        val result = CompletableDeferred<String>()
        val input = document.createElement("input") as? HTMLInputElement ?: (return "")
        input.type = "file"
        input.accept = "image/"

        input.onchange = {
            val file = input.files?.get(0)

            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    result.complete(reader.result as? String ?: "")
                }
                reader.readAsDataURL(file)
            } else result.complete("")
        }

        input.click()
        return result.await()
    }

    suspend fun onClipboardClick(): String {
        val deferred = CompletableDeferred<String>()

        ClipboardUtils.readImage { data, error ->
            when {
                error != null -> console.log(error)
                data != null -> deferred.complete(data)
                else -> console.log("Image is not available – copy an image to clipboard.")
            }
        }

        return deferred.await()
    }

    fun reloadData() {
        AppScope.scope.launch {
            val newFolders = mutableListOf<Bookmarks>()

            (uiState.value.openFolders + listOf("")).distinct().forEach {
                val subfolders = container.browserDatabase.getSpecificFolders(this, it)
                subfolders.forEach { s ->
                    newFolders.addAll(container.browserDatabase.getSpecificFolders(this, s.uuid))
                }
                newFolders.addAll(subfolders)
            }

            _uiState.update {
                it.copy(
                    folders = newFolders.distinctBy { f -> f.uuid },
                    folderContent = if (popup)
                        emptyList()
                    else container.browserDatabase.getChilds(this, it.selectedFolder)
                        .createImageIfNotExist().sortedBy { c -> c.index },
                    selectedElements = emptyList()
                )
            }
        }
    }

    fun exportBookmarks() {
        val exportManager = ExportManager(container.browserDatabase)

        exportManager.exportBookmarkFile()
    }

    fun exportVivaldiImages() {
        val exportManager = ExportManager(container.browserDatabase)

        exportManager.exportVivaldiImages()
    }

    fun importBookmarks() {
        importManager.importBookmarksFile()
    }

    fun importOperaImages() {
        importManager.importOperaImages()
    }

    fun importVivaldiImages() {
        importManager.importVivaldiImages()
    }

    fun getFolderPath(folder: String): Map<String, String> {
        val path = mutableListOf<Pair<String, String>>()
        var currentId = folder

        while (currentId.isNotEmpty()) {
            val f = uiState.value.folders.firstOrNull { it.uuid == currentId } ?: break
            path.add(f.uuid to f.name)
            currentId = f.parentId
        }

        return path.reversed().toMap()
    }

    fun deleteDB() {
        AppScope.scope.launch {
            try {
                container.browserDatabase.deleteDB()
            } finally {
                window.location.reload()
            }
        }
    }

    fun onNavElementClick(bookmark: Bookmarks?, uuid: String) {
        if (bookmark != null && bookmark.type == Constants.URL)
            window.open(bookmark.url, "_blank")
        else {
            AppScope.scope.launch {
                val isOpen = uiState.value.openFolders.contains(uuid)

                if (!isOpen)
                    container.browserDatabase.getBookmark(this, uuid)
                        ?.let { onNavElementFoldClick(it.parentId, isOpen) }

                _uiState.update {
                    it.copy(
                        selectedFolder = uuid,
                        folderContent = if (popup)
                            emptyList()
                        else container.browserDatabase.getChilds(this, uuid).createImageIfNotExist()
                            .sortedBy { c -> c.index },
                        selectedElements = emptyList()
                    )
                }
                window.localStorage[Constants.LAST_OPEN_FOLDER] = uuid
            }
        }
    }

    fun onNavElementFoldClick(uuid: String, open: Boolean? = null) {
        AppScope.scope.launch {
            val isOpen = open ?: uiState.value.openFolders.contains(uuid)

            val childFolders = container.browserDatabase.getSpecificFolders(this, uuid)
            val newFolders = mutableListOf<Bookmarks>()

            childFolders.forEach {
                newFolders.add(it)
                newFolders.addAll(container.browserDatabase.getSpecificFolders(this, it.uuid))
            }

            _uiState.update {
                it.copy(
                    openFolders = if (uuid.isNotEmpty()) {
                        if (isOpen) it.openFolders - uuid else (it.openFolders + uuid).distinct()
                    } else it.openFolders,
                    folders = if (uuid.isNotEmpty()) {
                        if (isOpen) it.folders else (it.folders + newFolders).distinctBy { f -> f.uuid }
                    } else it.folders
                )
            }
        }
    }

    fun changeTextColor(color: Int) {
        _uiState.update {
            it.copy(textColor = color)
        }
        window.localStorage[Constants.TEXT_COLOR] = color.toString()
    }

    fun changeCardSize(size: Int) {
        _uiState.update {
            it.copy(cardSize = size)
        }
        window.localStorage[Constants.CARD_SIZE] = size.toString()
    }

    fun changeDarkening(darkening: Boolean) {
        _uiState.update {
            it.copy(darkening = darkening)
        }
        window.localStorage[Constants.DARKENING] = if (darkening) Constants.TRUE else Constants.FALSE
    }

    fun List<Bookmarks>.createImageIfNotExist(): List<Bookmarks> {
        return this.map {
            if (it.image.isEmpty())
                it.copy(
                    image = "https://www.google.com/s2/favicons?sz=64&domain_url=${it.url}&size=256"
                )
            else it
        }
    }
}