package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import org.napharcos.bookmarkmanager.ImportManager
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState

@Composable
fun DialogSummary(
    uiState: UiState,
    viewModel: OptionsViewModel
) {
    when {
        uiState.showingChangeBackgroundDialog ->
            ChangeBackgroundDialog(
                viewModel = viewModel,
                uiState = uiState,
                onClose = { viewModel.updateShowingChangeBackground(false) },
                onConfirm = { viewModel.onBackgroundConfirmClick(it) }
            )

        uiState.showindAddNewElementDialog ->
            NewElementDialog(
                viewModel = viewModel,
                onCancel = { viewModel.updateShowingNewElement(false) },
                onConfirm = { type, name, url, image -> viewModel.onAddNewElementConfirmClick(type, name, url, image) }
            )

        uiState.editElement != null ->
            EditElementDialog(
                viewModel = viewModel,
                bookmark = uiState.editElement,
                onCancel = { viewModel.updateEditElement(null) },
                onConfirm = { bookmark, name, url, image -> viewModel.onEditConfirmClick(bookmark, name, url, image) }
            )

        uiState.trashElements.isNotEmpty() ->
            ConfirmDialog(
                title = getString(Values.MOVE_TRASH_TITLE),
                text = getString(Values.MOVE_TRASH_TEXT, uiState.trashElements.size),
                onClose = { viewModel.clearTrashElements() },
                onConfirm = {
                    viewModel.moveElementToFolder(
                        element = uiState.trashElements.firstOrNull { it !in uiState.selectedElements } ?: uiState.trashElements[0],
                        target = Constants.TRASH,
                        confirmed = true,
                        onlyOne = false
                    )
                }
            )

        uiState.showingClearTrashDialog ->
            ConfirmDialog(
                title = getString(Values.DELETE_TITLE),
                text = getString(Values.CLEAR_TRASH),
                onClose = { viewModel.updateShowingClearTrash(false) },
                onConfirm = { viewModel.clearTrash() }
            )

        uiState.deleteElements.isNotEmpty() ->
            ConfirmDialog(
                title = getString(Values.DELETE_TITLE),
                text = getString(Values.DELETE_TEXT, uiState.deleteElements.size),
                onClose = { viewModel.updateDeleteElements(emptyList()) },
                onConfirm = { viewModel.deleteElements() }
            )

        uiState.showingImportBookmarksDialog ->
            ImportDialog(
                viewModel = viewModel,
                onClose = { viewModel.updateShowingImportDialog(false) }
            )
    }

    if (ImportManager.isLoading)
        LoadingDialog(ImportManager.loadingText)
}