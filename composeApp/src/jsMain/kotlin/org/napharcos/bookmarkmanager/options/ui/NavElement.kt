package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.UiState
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.elementButton
import org.napharcos.bookmarkmanager.options.toTextColor

@Composable
fun NavElement(
    uuid: String,
    folderName: String,
    selected: Boolean,
    onFoldClick: () -> Unit,
    onElementClick: () -> Unit,
    viewModel: ViewModel,
    uiState: UiState
) {
    var onEnter by remember { mutableStateOf(false) }
    val regex = listOf("\u23F7", "\u23F5").find { folderName.contains(it) }
    val names = regex?.let { folderName.split(it) } ?: listOf(folderName)

    var onFoldEnter by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                justifyContent(JustifyContent.Start)
                width(100.percent)
                height(30.px)
                borderRadius(8.px)
                border(1.px, LineStyle.Solid, Color.transparent)
                backgroundColor(if (onEnter) rgba(0, 0, 139, 0.9) else if (selected) rgba(25, 25, 112, 0.9) else Color.transparent)
            }
            onDragOver { it.preventDefault() }
            onDrop {
                it.preventDefault()
                val draggedId = it.dataTransfer?.getData("uuid") ?: return@onDrop
                viewModel.moveElementToFolder(draggedId, uuid)
            }
        }
    ) {
        if (names.size > 1) {
            Button(
                attrs = {
                    style {
                        elementButton(false)
                        paddingRight(0.px)
                        color(if (onFoldEnter) Color.cyan else uiState.textColor.toTextColor())
                    }
                    onClick { onFoldClick() }
                    onMouseEnter {
                        onFoldEnter = true
                        onEnter = true
                    }
                    onMouseLeave {
                        onFoldEnter = false
                        onEnter = false
                    }
                }
            ) {
                Text(names[0] + regex)
            }
        }
        Button(
            attrs = {
                style {
                    flex(1)
                    elementButton(false)
                    paddingLeft(0.px)
                    color(uiState.textColor.toTextColor())
                }
                onClick { onElementClick() }
                onMouseEnter { onEnter = true }
                onMouseLeave { onEnter = false }
            }
        ) {
            Text(if (names.size > 1) names[1] else " ${names[0]}")
        }
    }
}