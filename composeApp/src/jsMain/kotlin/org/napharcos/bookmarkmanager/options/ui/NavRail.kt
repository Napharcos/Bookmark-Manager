package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState

@Composable
fun NavRail(
    viewModel: OptionsViewModel,
    uiState: UiState,
    height: Int
) {
    val title = remember { getString(Values.BOOKMARKS) }

    Div(
        attrs = {
            style {
                width(15.percent)
                minWidth(200.px)
                maxWidth(400.px)
                height(height.px)
                if (!uiState.darkening) backgroundColor(Color.transparent)
                else backgroundColor(rgba(50, 50, 50, 0.6))
                paddingLeft(8.px)
                paddingRight(8.px)
            }
        }
    ) {
        H2(
            attrs = {
                style {
                    paddingTop(8.px)
                    paddingBottom(8.px)
                    paddingLeft(16.px)
                    marginBottom(32.px)
                    cursor("pointer")
                }
                onClick { viewModel.onNavElementClick(null, "") }
                onDragOver { it.preventDefault() }
                onDrop {
                    it.preventDefault()
                    val draggedId = it.dataTransfer?.getData("uuid") ?: return@onDrop
                    viewModel.moveElementToFolder(draggedId, "")
                }
            }
        ) {
            Text(title)
        }
        BookmarksNavTree(viewModel, uiState, height)
    }
}