package org.napharcos.bookmarkmanager

import org.napharcos.bookmarkmanager.popup.PageData

data class UiState(
    val background: String = "",
    val textColor: Int = 190,
    val cardSize: Int = 300,
    val darkening: Boolean = true,
    val selectedFolder: String = "",
    val openFolders: List<String> = emptyList(),
    val folders: List<Bookmarks> = emptyList(),
    val folderContent: List<Bookmarks> = emptyList(),
    val showingChangeBackgroundDialog: Boolean = false,
    val showingImportBookmarksDialog: Boolean = false,
    val showingExportBookmarksDialog: Boolean = false,
    val showindAddNewElementDialog: Boolean = false,
    val showingClearTrashDialog: Boolean = false,
    val trashElements: List<String> = emptyList(),
    val editElement: Bookmarks? = null,
    val selectedElements: List<String> = emptyList(),
    val deleteElements: List<String> = emptyList(),
    val pageData: PageData? = null,
    val newFolder: String? = null
)