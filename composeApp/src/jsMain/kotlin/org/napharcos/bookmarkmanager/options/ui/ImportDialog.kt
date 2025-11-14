package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.ImportManager
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString

@Composable
fun ImportDialog(
    viewModel: ViewModel,
    onClose: () -> Unit
) {
    var cancelEntered by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style { dialogBackground() }
            onClick { onClose() }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    justifyContent(JustifyContent.SpaceBetween)
                    backgroundColor(rgb(60, 60, 60))
                    borderRadius(12.px)
                    padding(8.px)
                    width(450.px)
                    height(250.px)
                    textAlign("center")
                    property("box-shadow", "rgba(0, 0, 0, 0.3) 0px 4px 12px")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        justifyContent(JustifyContent.Center)
                        alignContent(AlignContent.Center)
                    }
                }
            ) {
                H3(
                    attrs = {
                        style {
                            height(25.px)
                            fontSize(1.2.em)
                            textAlign("center")
                        }
                    }
                ) {
                    Text(getString(Values.IMPORT_BOOKMARKS))
                }
                ImportElement(
                    text = getString(Values.IMPORT_BOOKMARKS_FILE),
                    tooltip = ImportManager.BOOKMARK_FILE_PATHS.trimMargin(),
                    buttonText = getString(Values.BROWSE),
                    onClick = { viewModel.importBookmarks() }
                )
                ImportElement(
                    text = getString(Values.IMPORT_BOOKMARKS_IMAGE_OPERA),
                    tooltip = ImportManager.OPERA_IMAGE_FILE_PATH.trimMargin(),
                    buttonText = getString(Values.BROWSE),
                    onClick = { viewModel.importOperaImages() }
                )
                ImportElement(
                    text = getString(Values.IMPORT_BOOKMARKS_IMAGE_VIVALDI),
                    tooltip = ImportManager.VIVALDI_IMAGE_FOLDER_PATH.trimMargin(),
                    buttonText = getString(Values.BROWSE),
                    onClick = { viewModel.importVivaldiImages() }
                )
            }
            Button(
                attrs = {
                    onClick { onClose() }
                    style {
                        width(100.percent)
                        dialogButton(cancelEntered, false)
                    }
                    onMouseEnter { cancelEntered = true }
                    onMouseLeave { cancelEntered = false }
                }
            ) {
                Text(getString(Values.CANCEL))
            }
        }
    }
}

@Composable
fun ImportElement(
    text: String,
    tooltip: String,
    buttonText: String,
    onClick: () -> Unit
) {
    var showingInfo by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                width(100.percent)
                height(40.px)
                padding(4.px)
                alignItems(AlignItems.Center)
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    flexBasis(0.px)
                    flexGrow(3)
                    fontSize(1.1.em)
                    textAlign("left")
                }
            }
        ) {
            Text(text)
        }
        Button(
            attrs = {
                style {
                    flexBasis(0.px)
                    flexGrow(1)
                    marginLeft(8.px)
                    marginRight(8.px)
                    position(Position.Relative)
                    display(DisplayStyle.InlineBlock)
                }
                onClick { onClick() }
                onMouseEnter { showingInfo = true }
                onMouseLeave { showingInfo = false }
            }
        ) {
            Text(buttonText)

            Span(
                attrs = {
                    style {
                        visibility(if (showingInfo) VisibilityStyle.Visible else VisibilityStyle.Hidden)
                        width(620.px)
                        backgroundColor(rgb(27, 27, 27))
                        color(rgb(190, 190, 190))
                        borderRadius(6.px)
                        position(Position.Absolute)
                        top((-5).px)
                        left(105.percent)
                        property("z-index", 1)
                        whiteSpace("pre-wrap")
                        textAlign("left")
                        padding(8.px)
                    }
                }
            ) {
                Text(tooltip)
            }
        }
    }
}