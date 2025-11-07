package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.css.AlignContent
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.VisibilityStyle
import org.jetbrains.compose.web.css.alignContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexBasis
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.visibility
import org.jetbrains.compose.web.css.whiteSpace
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.ImportManager
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState

@Composable
fun ImportDialog(
    viewModel: OptionsViewModel,
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