package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString

@Composable
fun ExportDialog(
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
                    Text(getString(Values.EXPORT_BOOKMARKS))
                }
                ImportElement(
                    text = getString(Values.EXPORT_BOOKMARKS_TEXT),
                    tooltip = getString(Values.NOT_OPERA_COMPATIBLE),
                    buttonText = getString(Values.EXPORT),
                    onClick = { viewModel.exportBookmarks() },
                    tooltipWidth = 280
                )
                ImportElement(
                    text = getString(Values.EXPORT_VIVALDI_IMAGE),
                    tooltip = getString(Values.RECOMMAND_NEW_FOLDER),
                    buttonText = getString(Values.EXPORT),
                    onClick = { viewModel.exportVivaldiImages() },
                    tooltipWidth = 280
                )
                ImportElement(
                    text = getString(Values.DELETE_DB_TEXT),
                    tooltip = null,
                    buttonText = getString(Values.DELETE),
                    onClick = { viewModel.updateShowingDeleteDBDialog(true) }
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