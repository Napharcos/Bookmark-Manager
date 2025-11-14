package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.UiState
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString

@Composable
fun TopbarContent(
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
                    padding(8.px)
                    paddingTop(4.px)
                    width(100.percent)
                    height(90.percent)
                }
            }
        ) {
            LeftElements()
            LeftElementsContent(uiState, viewModel)
            Space()
            Space()
            Space()
            RightElements(viewModel, uiState)
        }
    }
    FolderPath(uiState, viewModel)
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
fun RightElements(
    viewModel: ViewModel,
    uiState: UiState
) {
    Div(
        attrs = {
            style {
                flexGrow(1)
                maxWidth(150.px)
                paddingLeft(8.px)
                paddingRight(8.px)
            }
        }
    ) {
        ElementButton(getString(Values.CHANGE_BACKGROUND)) { viewModel.updateShowingChangeBackground(true) }
        ElementButton(getString(Values.IMPORT_BOOKMARKS)) { viewModel.updateShowingImportDialog(true) }
        ElementButton("Könyjelzők exportálása") { viewModel.deleteDB() }
        ElementButton(getString(
            if (uiState.selectedFolder != Constants.TRASH) Values.ADD_NEW_ELEMENT else Values.CLEAR_TRASH_BUTTON
        )) {
            if (uiState.selectedFolder != Constants.TRASH)
                viewModel.updateShowingNewElement(true)
            else viewModel.updateShowingClearTrash(true)
        }
    }
}

@Composable
fun LeftElements() {
    Div(
        attrs = {
            style {
                flexGrow(0.5)
                maxWidth(150.px)
                paddingRight(8.px)
                paddingLeft(8.px)
            }
        }
    ) {
        ElementDescription(Values.TEXT_COLOR)
        ElementDescription(Values.CARD_SIZE)
        ElementDescription(Values.DARKENED_BACKGROUND)
    }
}

@Composable
fun Space() {
    Div(
        attrs = {
            style {
                flexGrow(1)
                maxWidth(300.px)
            }
        }
    )
}

@Composable
fun LeftElementsContent(
    uiState: UiState,
    viewModel: ViewModel
) {
    Div(
        attrs = {
            style {
                flexGrow(1)
                maxWidth(300.px)
            }
        }
    ) {
        Slider(uiState.textColor, 0, 255) { viewModel.changeTextColor(it) }
        Slider(uiState.cardSize, 100, 1200) { viewModel.changeCardSize(it) }
        SlideButton(uiState.darkening) { viewModel.changeDarkening(it) }
    }
}

@Composable
fun ElementButton(
    text: String,
    onClick: () -> Unit
) {
    Div {
        Button(
            attrs = {
                style {
                    margin(2.px)
                    height(24.px)
                    width(200.px)
                }
                onClick { onClick() }
            }
        ) {
            Text(text)
        }
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
fun SlideButton(
    active: Boolean,
    onChange: (Boolean) -> Unit
) {
    Div(
        attrs = {
            onClick { onChange(!active) }
            style {
                width(42.px)
                height(20.px)
                borderRadius(20.px)
                backgroundColor(if (active) Color.limegreen else Color.gray)
                position(Position.Relative)
                cursor("pointer")
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    width(18.px)
                    height(16.px)
                    borderRadius(50.percent)
                    backgroundColor(Color.white)
                    position(Position.Absolute)
                    left(if (active) 22.px else 2.px)
                    top(2.px)
                    property("transition", "left 0.2s ease")
                }
            }
        )
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