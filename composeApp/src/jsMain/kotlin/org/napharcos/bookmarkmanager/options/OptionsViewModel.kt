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
import org.napharcos.bookmarkmanager.AppScope
import org.napharcos.bookmarkmanager.Bookmark
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.ImportManager
import org.napharcos.bookmarkmanager.container.Container
import org.napharcos.bookmarkmanager.copy
import org.napharcos.bookmarkmanager.data.Constants
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.text.ifEmpty
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
                    folderContent = container.browserDatabase.getChilds(this, "").createImageIfNotExist(),
                    selectedElements = emptyList()
                )
            }
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

//    fun deleteElement(element: Bookmarks?) {
//        _uiState.update {
//            it.copy(deleteElement = element)
//        }
//    }

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

    fun onEditConfirmClick(bookmark: Bookmarks, name: String, url: String, image: String) {
        AppScope.scope.launch {
            val newBookmark = bookmark.copy(
                name = name.ifEmpty { bookmark.name },
                url = url.ifEmpty { bookmark.url },
                image = image.ifEmpty { bookmark.image }
            )

            container.browserDatabase.addBookmark(newBookmark, true)
            container.serverDatabase?.addBookmark(newBookmark, true)
            reloadData()
            updateEditElement(null)
        }
    }

    fun onDeleteConfirmClick(bookmark: Bookmarks) {
        AppScope.scope.launch {
            val newBookmark = bookmark.copy(
                parentId = Constants.TRASH,
                undoTrash = bookmark.parentId,
                index = getNextIndex(this, Constants.TRASH)
            )
            container.browserDatabase.addBookmark(newBookmark, true)
            container.serverDatabase?.addBookmark(newBookmark, true)
            reloadData()
            //deleteElement(null)
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
                    folderContent = container.browserDatabase.getChilds(this, it.selectedFolder).createImageIfNotExist(),
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
                        folderContent = container.browserDatabase.getChilds(this, uuid).createImageIfNotExist(),
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