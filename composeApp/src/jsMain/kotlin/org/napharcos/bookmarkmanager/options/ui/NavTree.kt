package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.napharcos.bookmarkmanager.BookmarksTree
import org.napharcos.bookmarkmanager.buildTree
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.folderNameBuilder
import org.napharcos.bookmarkmanager.getString
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState

@Composable
fun BookmarksNavTree(
    viewModel: OptionsViewModel,
    uiState: UiState,
    height: Int
) {
    Div(
        attrs = {
            style {
                width(100.percent)
                maxHeight((height - 85).px)
                overflowY("auto")
                overflowX("hidden")
                paddingRight(4.px)
                paddingLeft(4.px)
                marginRight(8.px)
                property("scrollbar-color", "#555 #3b3b3b")
            }
        }
    ) {
        NavTree(
            viewModel = viewModel,
            openFolders = uiState.openFolders,
            list = uiState.folders.buildTree(),
            depth = 0,
            uiState = uiState
        )
        NavElement(
            uuid = Constants.TRASH,
            folderName = "\uD83D\uDDD1\uFE0F " + getString(Values.TRASH),
            selected = uiState.selectedFolder == Constants.TRASH,
            onFoldClick = {},
            onElementClick = { viewModel.onNavElementClick(null, Constants.TRASH) },
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
fun NavTree(
    viewModel: OptionsViewModel,
    uiState: UiState,
    openFolders: List<String>,
    list: List<BookmarksTree>,
    depth: Int,
) {
    list.sortedBy { it.folder.index }.forEach {
        val isOpen = it.folder.uuid in openFolders

        NavElement(
            uuid = it.folder.uuid,
            folderName = folderNameBuilder(depth, it.folder.name, isOpen, it.children.isNotEmpty()),
            selected = uiState.selectedFolder == it.folder.uuid,
            onFoldClick = { viewModel.onNavElementFoldClick(it.folder.uuid) },
            onElementClick = { viewModel.onNavElementClick(it.folder, it.folder.uuid) },
            viewModel = viewModel,
            uiState = uiState
        )

        if (isOpen) {
            NavTree(
                viewModel = viewModel,
                openFolders = openFolders,
                list = it.children,
                depth = depth + 1,
                uiState = uiState
            )
        }
    }
}