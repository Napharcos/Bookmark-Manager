package org.napharcos.bookmarkmanager.options

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.container.Container
import org.napharcos.bookmarkmanager.data.Constants
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class OptionsViewModel(private val container: Container) {

    val importManager = ImportManager(container.browserDatabase, container.serverDatabase)

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        initUIState()
    }

    fun initUIState() {
        _uiState.update {
            it.copy(
                background = window.localStorage[Constants.BACKGROUND] ?: "",
                darkening = window.localStorage[Constants.DARKENING] != Constants.FALSE,
                textColor = window.localStorage[Constants.TEXT_COLOR]?.toInt() ?: it.textColor,
                cardSize = window.localStorage[Constants.CARD_SIZE]?.toInt() ?: it.cardSize
            )
        }

        AppScope.scope.launch {
            val childs = container.browserDatabase.getSpecificFolders(this, "")
            val newFolders = mutableListOf<Bookmarks>()

            childs.forEach {
                newFolders.add(it)
                newFolders.addAll(container.browserDatabase.getSpecificFolders(this, it.uuid))
            }

            _uiState.update {
                it.copy(
                    openFolders = listOf(""),
                    selectedFolder = "",
                    folders = newFolders,
                    folderContent = container.browserDatabase.getChilds(this, "").createImageIfNotExist().sortedBy { c -> c.index },
                    selectedElements = emptyList()
                )
            }
        }
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
            it.copy(showindAddNewElementDialog = showing)
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

    fun onSelectElementClick(select: Boolean, bookmark: String) {
        _uiState.update {
            it.copy(
                selectedElements = if (select) it.selectedElements + bookmark else it.selectedElements - bookmark
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

    fun clearTrash() {
        AppScope.scope.launch {
            val browserTrashElements = container.browserDatabase.getChilds(this, Constants.TRASH)
            val serverTrashElements = container.serverDatabase?.getChilds(this, Constants.TRASH)

            val allElements = (browserTrashElements.map { it.uuid } + (serverTrashElements?.map { it.uuid } ?: emptyList()))
                .distinct()

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
        val serverChilds = container.serverDatabase?.getChilds(scope, element)

        container.browserDatabase.deleteBookmark(scope, element)
        container.serverDatabase?.deleteBookmark(scope, element)

        val allChilds = (browserChilds.map { it.uuid } + (serverChilds?.map { it.uuid } ?: emptyList()))
            .distinct()

        allChilds.forEach { child ->
            deleteElementsRecursive(scope, child, visited)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onAddNewElementConfirmClick(type: String, name: String, url: String, image: String) {
        AppScope.scope.launch {
            val parent = uiState.value.selectedFolder

            val newBookmark = Bookmark(
                parentId = parent,
                name = name.ifEmpty { url.substringAfter("//").substringBefore("/") },
                type = type,
                url = url,
                index = getNextIndex(this, parent),
                imageId = if (image.isNotEmpty()) Uuid.random().toHexString() else "",
                image = if (image.isEmpty() && type == Constants.FOLDER) "./folder.svg" else image
            )

            container.browserDatabase.addBookmark(newBookmark)
            container.serverDatabase?.addBookmark(newBookmark)
            reloadData()
            updateShowingNewElement(false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onEditConfirmClick(bookmark: Bookmarks, name: String, url: String, image: String) {
        AppScope.scope.launch {
            val newBookmark = bookmark.copy(
                name = name.ifEmpty { bookmark.name },
                url = url.ifEmpty { bookmark.url },
                imageId = if (image.isNotEmpty()) Uuid.random().toHexString() else bookmark.imageId,
                image = image.ifEmpty { bookmark.image }
            )

            container.browserDatabase.addBookmark(newBookmark, true)
            container.serverDatabase?.addBookmark(newBookmark, true)
            reloadData()
            updateEditElement(null)
        }
    }

    suspend fun getNextIndex(scope: CoroutineScope, parentId: String): Int {
        val browserChilds = container.browserDatabase.getChilds(scope, parentId)
        val serverChilds = container.serverDatabase?.getChilds(scope, parentId)

        return maxOf(
            browserChilds.maxByOrNull { it.index }?.index ?: 0,
            serverChilds?.maxByOrNull { it.index }?.index ?: 0
        ) + 1
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
                container.browserDatabase.addBookmark(newBookmark, true)
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

                container.browserDatabase.addBookmark(newBookmark, override = true)
                container.serverDatabase?.addBookmark(newBookmark, true)

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

    fun reloadData() {
        AppScope.scope.launch {
            val newFolders = mutableListOf<Bookmarks>()

            uiState.value.openFolders.forEach {
                val subfolders = container.browserDatabase.getSpecificFolders(this, it)
                if (it.isEmpty()) {
                    subfolders.forEach { s ->
                        newFolders.addAll(container.browserDatabase.getSpecificFolders(this, s.uuid))
                    }
                }
                newFolders.addAll(subfolders)
            }

            _uiState.update {
                it.copy(
                    folders = newFolders.distinctBy { f -> f.uuid },
                    folderContent = container.browserDatabase.getChilds(this, it.selectedFolder).createImageIfNotExist().sortedBy { c -> c.index },
                    selectedElements = emptyList()
                )
            }
        }
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
            container.browserDatabase.deleteDB()
            reloadData()
        }
    }

    fun onNavElementClick(bookmark: Bookmarks?, uuid: String) {
        if (bookmark != null && bookmark.type == Constants.URL)
            window.open(bookmark.url, "_blank")
        else
            AppScope.scope.launch {
                _uiState.update {
                    it.copy(
                        selectedFolder = uuid,
                        folderContent = container.browserDatabase.getChilds(this, uuid).createImageIfNotExist().sortedBy { c -> c.index },
                        selectedElements = emptyList()
                    )
                }
            }
    }

    fun onNavElementFoldClick(uuid: String) {
        AppScope.scope.launch {
            val isOpen = uiState.value.openFolders.contains(uuid)

            val childFolders = container.browserDatabase.getSpecificFolders(this, uuid)
            val newFolders = mutableListOf<Bookmarks>()

            childFolders.forEach {
                newFolders.add(it)
                newFolders.addAll(container.browserDatabase.getSpecificFolders(this, it.uuid))
            }

            _uiState.update {
                it.copy(
                    openFolders = if (uuid.isNotEmpty()) { if (isOpen) it.openFolders - uuid else it.openFolders + uuid } else it.openFolders,
                    folders = if (uuid.isNotEmpty()) { if (isOpen) it.folders else (it.folders + newFolders).distinctBy { f -> f.uuid } } else it.folders
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