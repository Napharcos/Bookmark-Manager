package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.BackupManager
import org.napharcos.bookmarkmanager.UiState
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString

@Composable
fun TopbarMain(
    uiState: UiState,
    viewModel: ViewModel
) {
    Div(
        attrs = {
            style {
                position(Position.Absolute)
                top(0.px)
                left(0.px)
                width(100.percent)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    justifyContent(JustifyContent.SpaceBetween)
                    padding(8.px)
                    paddingTop(4.px)
                    width(100.percent)
                    height(84.px)
                }
            }
        ) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    justifyContent(JustifyContent.Start)
                }
            }) {
                LeftElements()
                LeftElementsContent(uiState, viewModel)
            }
            RightElements(uiState, viewModel)
        }
    }
    FolderPath(uiState, viewModel)
}

@Composable
fun RightElements(
    uiState: UiState,
    viewModel: ViewModel
) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.End)
        }
    }) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    marginRight(8.px)
                }
            }
        ) {
            IconElement(
                darkening = uiState.darkening,
                icon = folderManagerIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.CHANGE_BACKUP_FOLDER),
                onClick = { BackupManager.changeBackupFolder { viewModel.reloadData() } }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = importIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.IMPORT_BOOKMARKS),
                onClick = { viewModel.updateShowingImportDialog(true) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = exportIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.EXPORT_BOOKMARKS),
                onClick = { viewModel.updateShowingExportDialog(true) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = backgroundIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.CHANGE_BACKGROUND),
                onClick = { viewModel.updateShowingChangeBackground(true) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = if (uiState.darkening) contrastOffIcon(uiState.textColor.toHex()) else contrastIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.DARKENED_BACKGROUND),
                onClick = { viewModel.changeDarkening(!uiState.darkening) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = termsIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.TERMS),
                onClick = { viewModel.updateShowingTerms(true) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = librariesIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.LIBRARIES),
                onClick = { viewModel.updateShowingLibraries(true) }
            )
            IconElement(
                darkening = uiState.darkening,
                icon = githubIcon(uiState.textColor.toHex()),
                tooltip = getString(Values.SOURCE_CODE),
                onClick = { window.open("https://github.com/Napharcos/Bookmark-Manager", "_blank") },
                last = true
            )
        }
        IconTextElement(
            darkening = uiState.darkening,
            icon = if (uiState.selectedFolder != Constants.TRASH) addIcon(uiState.textColor.toHex()) else trashIcon(
                uiState.textColor.toHex()
            ),
            text = getString(if (uiState.selectedFolder != Constants.TRASH) Values.ADD_NEW_ELEMENT else Values.CLEAR_TRASH_BUTTON),
            onClick = {
                if (uiState.selectedFolder != Constants.TRASH)
                    viewModel.updateShowingNewElement(true)
                else viewModel.updateShowingClearTrash(true)
            }
        )
    }
}

@Composable
fun IconElement(
    darkening: Boolean,
    icon: String,
    tooltip: String? = null,
    tooltipWidth: Int = 72,
    onClick: () -> Unit,
    last: Boolean = false
) {
    var entered by remember { mutableStateOf(false) }

    Div(attrs = {
        style {
            position(Position.Relative)
            width(25.px)
            height(25.px)
            marginRight(12.px)
        }
    }) {
        Div(
            attrs = {
                style {
                    position(Position.Absolute)
                    iconButtonStyle(withColor = false)
                    backgroundColor(
                        if (darkening) when (entered) {
                            true -> rgba(50, 50, 50, 0.8)
                            false -> rgba(50, 50, 50, 0)
                        } else when (entered) {
                            true -> rgba(50, 50, 50, 1)
                            false -> rgba(50, 50, 50, 0.4)
                        }
                    )
                }
                onClick { onClick() }
                onMouseEnter { entered = true }
                onMouseLeave { entered = false }
            }
        ) {
            key(icon) {
                Div(
                    attrs = {
                        ref {
                            it.innerHTML = icon
                            onDispose { }
                        }
                    }
                )
            }

            if (tooltip != null)
                Span(
                    attrs = {
                        style {
                            visibility(if (entered) VisibilityStyle.Visible else VisibilityStyle.Hidden)
                            width(tooltipWidth.px)
                            backgroundColor(rgb(27, 27, 27))
                            color(rgb(190, 190, 190))
                            borderRadius(6.px)
                            position(Position.Absolute)
                            top(105.percent)
                            left(if (last) (-22).percent else 50.percent)
                            marginLeft((-tooltipWidth / 2).px)
                            property("z-index", 1)
                            whiteSpace("pre-wrap")
                            textAlign("center")
                            padding(4.px)
                        }
                    }
                ) {
                    Text(tooltip)
                }
        }
    }
}

@Composable
fun IconTextElement(
    darkening: Boolean,
    icon: String,
    text: String,
    onClick: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            marginTop(10.px)
            height(25.px)
            alignItems(AlignItems.Center)
            borderRadius(12.px)
            width(148.px)
            backgroundColor(
                if (darkening) when (entered) {
                    true -> rgba(50, 50, 50, 0.8)
                    false -> rgba(50, 50, 50, 0)
                } else when (entered) {
                    true -> rgba(50, 50, 50, 1)
                    false -> rgba(50, 50, 50, 0.4)
                }
            )
            paddingLeft(1.px)
            paddingTop(3.px)
            paddingRight(3.px)
            paddingBottom(3.px)
            cursor("pointer")
            marginRight(12.px)
        }
        onClick { onClick() }
        onMouseEnter { entered = true }
        onMouseLeave { entered = false }
    }) {
        key(icon) {
            Div(attrs = {
                style {
                    width(25.px)
                    padding(4.px)
                }
                ref {
                    it.innerHTML = icon
                    onDispose { }
                }
            })
        }
        Div(attrs = {
            style {
                height(25.px)
                marginTop(4.px)
                justifyContent(JustifyContent.Center)
            }
        }
        ) {
            Text(text)
        }
    }
}


@Composable
fun FolderPath(
    uiState: UiState,
    viewModel: ViewModel
) {
    Div(
        attrs = {
            style {
                position(Position.Absolute)
                bottom(0.px)
                left(0.px)
                width(100.percent)
                paddingRight(4.px)
                paddingLeft(4.px)
                paddingTop(4.px)
                height(24.px)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                }
            }
        ) {
            viewModel.getFolderPath(uiState.selectedFolder).forEach { (id, name) ->
                FolderPathElement(name) { viewModel.onNavElementClick(null, id) }
                BetweenFolderPathElemens()
            }
        }
    }
}

@Composable
fun LeftElements() {
    Div(
        attrs = {
            style {
                width(150.px)
                paddingRight(8.px)
                paddingLeft(8.px)
            }
        }
    ) {
        ElementDescription(Values.TEXT_COLOR)
        ElementDescription(Values.CARD_SIZE)
    }
}

@Composable
fun ElementDescription(
    textKey: String
) {
    Div(
        attrs = {
            style {
                fontSize(1.2.em)
                height(22.px)
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Right)
                alignItems(AlignItems.Center)
            }
        }
    ) {
        Text(getString(textKey))
    }
}

@Composable
fun LeftElementsContent(
    uiState: UiState,
    viewModel: ViewModel
) {
    Div(
        attrs = {
            style {
                paddingRight(8.px)
                width(250.px)
            }
        }
    ) {
        Slider(uiState.textColor, 0, 255) { viewModel.changeTextColor(it) }
        Slider(uiState.cardSize, 100, 1200) { viewModel.changeCardSize(it) }
    }
}

@Composable
fun Slider(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onChange: (Int) -> Unit
) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
            }
        }
    ) {
        Input(
            type = InputType.Range,
            attrs = {
                min("$minValue")
                max("$maxValue")
                value(value.toString())
                onInput { event -> onChange(event.value?.toInt() ?: 0) }
                style {
                    width(100.percent)
                    maxWidth(300.px)
                    marginLeft(4.px)
                    backgroundColor(Color.lightgray)
                    color(Color.darkgray)
                    height(20.px)
                }
            }
        )
    }
}

@Composable
fun BetweenFolderPathElemens() {
    Div(
        attrs = {
            style {
                flexGrow(1)
                maxWidth(20.px)
                whiteSpace("pre")
                fontSize(1.1.em)
            }
        }
    ) {
        Text(" > ")
    }
}

@Composable
fun FolderPathElement(
    elementName: String,
    onElementClick: () -> Unit
) {
    Div(
        attrs = {
            style {
                flexGrow(1)
                maxWidth(150.px)
                textDecoration("underline")
                color(rgb(77, 163, 255))
                cursor("pointer")
                overflow("hidden")
                whiteSpace("nowrap")
                textAlign("center")
                fontSize(1.1.em)
                property("text-overflow", "ellipsis")
            }
            onClick { onElementClick() }
        }
    ) {
        Text(elementName)
    }
}

fun Int.toHex(): String {
    val hex = toString(16).padStart(2, '0')
    return hex + hex + hex
}