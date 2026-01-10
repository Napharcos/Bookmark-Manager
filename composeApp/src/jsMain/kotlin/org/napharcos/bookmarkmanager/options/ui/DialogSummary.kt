package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import org.napharcos.bookmarkmanager.BackupManager
import org.napharcos.bookmarkmanager.ExportManager
import org.napharcos.bookmarkmanager.ImportManager
import org.napharcos.bookmarkmanager.UiState
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.container.Container
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString

@Composable
fun DialogSummary(
    uiState: UiState,
    viewModel: ViewModel,
    container: Container
) {
    when {
        uiState.showingAddBackupFolderDialog ->
            InfoDialog(
                title = Values.ADD_BACKUP_TITLE,
                text = Values.ADD_BACKUP_TEXT,
                onConfirm = { BackupManager.changeBackupFolder() }
            )

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

        uiState.showingExportBookmarksDialog ->
            ExportDialog(
                viewModel = viewModel,
                onClose = { viewModel.updateShowingExportDialog(false) }
            )
    }

    if (uiState.showingDeleteDBDialog)
        DeleteDBDialog(
            onClose = { viewModel.updateShowingDeleteDBDialog(false) },
            onConfirm = { viewModel.deleteDB() }
        )

    if (ImportManager.isLoading)
        LoadingDialog(ImportManager.loadingText)

    if (ExportManager.isLoading)
        LoadingDialog(ExportManager.loadingText)

    when {
        ImportManager.duplicateUuid != null ->
            DuplicateImportDialog(
                newElement = ImportManager.duplicateUuid!!,
                newParentId = ImportManager.newParentId,
                database = container.browserDatabase,
                title = getString(Values.DUPLICATE_ID),
                cancelText = getString(Values.DROP),
                confirmText = getString(Values.OVERRIDE),
                thirdText = getString(Values.KEEPS_BOTH),
                enableThird = true,
                onCancel = {
                    if (it) ImportManager.manageDuplicateUuid = 0
                    ImportManager.duplicate?.complete(0)
                },
                onConfirm = {
                    if (it) ImportManager.manageDuplicateUuid = 1
                    ImportManager.duplicate?.complete(1)
                },
                onThird = {
                    if (it) ImportManager.manageDuplicateUuid = 2
                    ImportManager.duplicate?.complete(2)
                }
            )

        ImportManager.duplicateUrl != null ->
            DuplicateImportDialog(
                newElement = ImportManager.duplicateUrl!!,
                newParentId = ImportManager.newParentId,
                database = container.browserDatabase,
                title = getString(Values.DUPLICATE_URL),
                cancelText = getString(Values.DROP),
                confirmText = getString(Values.KEEPS_BOTH),
                thirdText = "",
                enableThird = false,
                onCancel = {
                    if (it) ImportManager.manageDuplicateUrl = 0
                    ImportManager.duplicate?.complete(0)
                },
                onConfirm = {
                    if (it) ImportManager.manageDuplicateUrl = 1
                    ImportManager.duplicate?.complete(1)
                },
                onThird = {}
            )
    }
}