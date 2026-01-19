package org.napharcos.bookmarkmanager.options

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.container.ContainerImpl
import org.napharcos.bookmarkmanager.options.ui.DialogSummary
import org.napharcos.bookmarkmanager.options.ui.FolderElementsList
import org.napharcos.bookmarkmanager.options.ui.NavRail
import org.napharcos.bookmarkmanager.options.ui.TopbarMain
import org.w3c.dom.events.Event

@Composable
fun OptionsSummary() {
    val container = remember { ContainerImpl() }
    val viewModel = remember { ViewModel(container, false) }
    val uiState by viewModel.uiState.collectAsState()

    var windowHeight by remember { mutableStateOf(window.innerHeight) }
    var windowWidth by remember { mutableStateOf(window.innerWidth) }

    DisposableEffect(Unit) {
        val resizeListener: (Event) -> Unit = {
            windowHeight = window.innerHeight
            windowWidth = window.innerWidth
        }

        val focusListener: (Event) -> Unit = {
            val visible = !(document.asDynamic().hidden as Boolean)
            if (visible)
                viewModel.reloadData()
        }

        window.addEventListener("resize", resizeListener)
        document.addEventListener("visibilitychange", focusListener)

        onDispose {
            window.removeEventListener("resize", resizeListener)
            document.removeEventListener("visibilitychange", focusListener)
        }
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

    DialogSummary(uiState, viewModel, container)
}

@Composable
fun MainPage(
    viewModel: ViewModel,
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
    viewModel: ViewModel
) {
    Div(
        attrs = {
            style {
                position(Position.Relative)
                width(100.percent)
                height(96.px)
                if (uiState.darkening) backgroundColor(rgba(50, 50, 50, 0.6))
            }
        }
    ) {
        TopbarMain(uiState, viewModel)
    }
}

fun Int.toTextColor(): CSSColorValue {
    return rgb(this, this, this)
}