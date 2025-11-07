package org.napharcos.bookmarkmanager.options

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.container.ContainerImpl
import org.napharcos.bookmarkmanager.options.ui.ChangeBackgroundDialog
import org.napharcos.bookmarkmanager.options.ui.ConfirmDialog
import org.napharcos.bookmarkmanager.options.ui.EditElementDialog
import org.napharcos.bookmarkmanager.options.ui.FolderElementsList
import org.napharcos.bookmarkmanager.options.ui.ImportDialog
import org.napharcos.bookmarkmanager.options.ui.LoadingDialog
import org.napharcos.bookmarkmanager.options.ui.NavRail
import org.napharcos.bookmarkmanager.options.ui.NewElementDialog
import org.napharcos.bookmarkmanager.options.ui.TopbarContent
import kotlin.math.roundToInt

@Composable
fun OptionsSummary() {
    val container = remember { ContainerImpl() }
    val viewModel = remember { OptionsViewModel(container) }
    val uiState by viewModel.uiState.collectAsState()

    var windowHeight by remember { mutableStateOf(window.innerHeight) }
    var windowWidth by remember { mutableStateOf(window.innerWidth) }

    LaunchedEffect(Unit) {
        window.addEventListener("resize", {
            windowHeight = window.innerHeight
            windowWidth = window.innerWidth
        })
    }

    LaunchedEffect(ImportManager.isLoading) {
        if (!ImportManager.isLoading)
            viewModel.reloadData()
    }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                width(windowWidth.px)
                height(windowHeight.px)
                if (uiState.background.isNotEmpty())
                    backgroundImage("url('${uiState.background}')")
                backgroundSize("cover")
                color(uiState.textColor.toTextColor())
                backgroundPosition("center")
                backgroundRepeat("no-repeat")
                flexDirection(FlexDirection.Row)
            }
        }
    ) {
        NavRail(viewModel, uiState, windowHeight)
        MainPage(viewModel, uiState)
    }
    if (uiState.showingChangeBackgroundDialog)
        ChangeBackgroundDialog(
            viewModel = viewModel,
            uiState = uiState,
            onClose = { viewModel.updateShowingChangeBackground(false) },
            onConfirm = { viewModel.onBackgroundConfirmClick(it)  }
        )

    if (uiState.showindAddNewElementDialog)
        NewElementDialog(
            viewModel = viewModel,
            onCancel = { viewModel.updateShowingNewElement(false) },
            onConfirm = { type, name, url, image -> viewModel.onAddNewElementConfirmClick(type, name, url, image) }
        )

    uiState.editElement?.let {
        EditElementDialog(
            viewModel = viewModel,
            bookmark = it,
            onCancel = { viewModel.updateEditElement(null) },
            onConfirm = { bookmark, name, url, image -> viewModel.onEditConfirmClick(bookmark, name, url, image) }
        )
    }

//    uiState.deleteElement?.let {
//        ConfirmDialog(
//            title = getString(),
//            text = getString(),
//            onClose = { viewModel.deleteElement(null) },
//            onConfirm = { viewModel.onDeleteConfirmClick(it) }
//        )
//    }

    if (uiState.showingImportBookmarksDialog)
        ImportDialog(viewModel) { viewModel.updateShowingImportDialog(false) }

    if (ImportManager.isLoading)
        LoadingDialog(ImportManager.loadingText)
}

@Composable
fun MainPage(
    viewModel: OptionsViewModel,
    uiState: UiState
) {
    Div(
        attrs = {
            style {
                width(100.percent)
                height(100.vh)
                backgroundColor(Color.transparent)
            }
        }
    ) {
        TopBar(uiState, viewModel)
        SplitLine()
        FolderElementsList(
            viewModel = viewModel,
            uiState = uiState,
            elements = uiState.folderContent,
            itemsSize = uiState.cardSize,
        )
    }
}

@Composable
fun TopBar(
    uiState: UiState,
    viewModel: OptionsViewModel
) {
    Div(
        attrs = {
            style {
                position(Position.Relative)
                width(100.percent)
                height(topbarHeight().px)
                if (uiState.darkening) backgroundColor(rgba(50, 50, 50, 0.6))
            }
        }
    ) {
        TopbarContent(uiState, viewModel)
    }
}

fun topbarHeight(): Int {
    return (window.innerHeight * (15.0 / 100.0)).roundToInt()
}

fun Int.toTextColor(): CSSColorValue {
    return rgb(this, this, this)
}