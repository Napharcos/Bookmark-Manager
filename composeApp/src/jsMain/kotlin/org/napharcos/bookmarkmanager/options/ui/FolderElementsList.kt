package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState
import org.napharcos.bookmarkmanager.options.topbarHeight
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun FolderElementsList(
    viewModel: OptionsViewModel,
    uiState: UiState,
    elements: List<Bookmarks>,
    itemsSize: Int,
) {
    var selectedElements by remember { mutableStateOf<List<String>>(emptyList()) }

    var isSelecting by remember { mutableStateOf(false) }
    var startX by remember { mutableStateOf(0.0) }
    var startY by remember { mutableStateOf(0.0) }
    var currentX by remember { mutableStateOf(0.0) }
    var currentY by remember { mutableStateOf(0.0) }

    var containerRef by remember { mutableStateOf<HTMLElement?>(null) }

    Div(
        attrs = {
            ref {
                containerRef = it
                onDispose { containerRef = null }
            }
            style {
                position(Position.Relative)

                height((window.innerHeight - topbarHeight()).px)
                width(100.percent)
                overflowY("auto")
                property("scrollbar-color", "#555 #3b3b3b")
                display(DisplayStyle.Grid)
                gridTemplateColumns("repeat(auto-fill, minmax(${itemsSize}px, 1fr))")
                gridAutoRows("${itemsSize}px")
                gap(8.px)
                boxSizing("border-box")
                paddingTop(8.px)
                paddingRight(8.px)
                paddingLeft(8.px)
                property("user-select", "none")
            }
            onMouseDown { e ->
                if (e.target != e.currentTarget) return@onMouseDown
                val rect = containerRef?.getBoundingClientRect() ?: return@onMouseDown
                isSelecting = true
                startX = e.clientX - rect.left
                startY = e.clientY - rect.top
                currentX = startX
                currentY = startY
            }
            onMouseMove { e ->
                if (isSelecting) {
                    val rect = containerRef?.getBoundingClientRect() ?: return@onMouseMove
                    currentX = e.clientX - rect.left
                    currentY = e.clientY - rect.top

                    val minX = min(startX, currentX)
                    val minY = min(startY, currentY)
                    val maxX = max(startX, currentX)
                    val maxY = max(startY, currentY)

                    val newList = mutableListOf<String>()

                    uiState.folderContent.filter { !uiState.selectedElements.contains(it.uuid) }.forEach { element ->
                        val elRect = document.getElementById(element.uuid)
                            ?.getBoundingClientRect() ?: return@forEach
                        val left = elRect.left - rect.left
                        val top = elRect.top - rect.top
                        val right = elRect.right - rect.left
                        val bottom = elRect.bottom - rect.top

                        if (left < maxX && right > minX && top < maxY && bottom > minY) {
                            newList.add(element.uuid)
                            selectedElements = newList
                        }
                    }
                }
            }
            onMouseUp { _ ->
                if (!isSelecting) return@onMouseUp
                isSelecting = false

                selectedElements.forEach {
                    viewModel.onSelectElementClick(true, it)
                }
                selectedElements = emptyList()
            }
        }
    ) {
        elements.forEach {
            FolderCardElement(
                uuid = it.uuid,
                image = it.image,
                name = it.name,
                url = it.url,
                modified = it.modified.toLong(),
                type = it.type,
                onClick = { viewModel.onNavElementClick(it, it.uuid) },
                size = itemsSize,
                selected = uiState.selectedElements.contains(it.uuid) || selectedElements.contains(it.uuid),
                onEditClick = { viewModel.updateEditElement(it) },
                onSelectClick = { s -> viewModel.onSelectElementClick(s, it.uuid) },
                onDeleteClick = { viewModel.onDeleteConfirmClick(it) },
            )

            if (isSelecting) {
                val left = min(startX, currentX).px
                val top = min(startY, currentY).px
                val width = abs(currentX - startX).px
                val height = abs(currentY - startY).px

                Div({
                    style {
                        position(Position.Absolute)
                        left(left)
                        top(top)
                        width(width)
                        height(height)
                        backgroundColor(rgba(0, 120, 255, 0.2))
                        border(1.px, LineStyle.Solid, Color.blue)
                        property("pointer-events", "none")
                        property("user-select", "none")
                    }
                })
            }
        }
    }
}