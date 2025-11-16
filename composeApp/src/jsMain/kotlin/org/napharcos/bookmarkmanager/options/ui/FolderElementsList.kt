package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.DragZone
import org.napharcos.bookmarkmanager.UiState
import org.napharcos.bookmarkmanager.ViewModel
import org.napharcos.bookmarkmanager.addPlaceholder
import org.napharcos.bookmarkmanager.data.Constants
import org.w3c.dom.HTMLElement
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun FolderElementsList(
    viewModel: ViewModel,
    uiState: UiState,
    elements: List<Bookmarks>,
    itemsSize: Int,
) {
    var currentElements by remember { mutableStateOf(elements) }
    var selectedElements by remember { mutableStateOf<List<String>>(emptyList()) }

    var isSelecting by remember { mutableStateOf(false) }
    var startX by remember { mutableStateOf(0.0) }
    var startY by remember { mutableStateOf(0.0) }
    var currentX by remember { mutableStateOf(0.0) }
    var currentY by remember { mutableStateOf(0.0) }

    var containerRef by remember { mutableStateOf<HTMLElement?>(null) }
    var draggingElement by remember { mutableStateOf("") }
    var dragZone by remember { mutableStateOf<DragZone?>(null) }

    LaunchedEffect(elements) {
        currentElements = elements
    }

    Div(
        attrs = {
            ref {
                containerRef = it
                onDispose { containerRef = null }
            }
            style {
                position(Position.Relative)
                height((window.innerHeight - 145).px)
                width(100.percent)
                overflowY("auto")
                overflowX("hidden")
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
                val rect = containerRef ?: return@onMouseDown
                isSelecting = true
                startX = e.clientX - rect.offsetLeft.toDouble()
                startY = e.clientY - rect.offsetTop + rect.scrollTop
                currentX = startX
                currentY = startY
            }
            onMouseMove { e ->
                if (isSelecting) {
                    val rect = containerRef ?: return@onMouseMove
                    currentX = e.clientX - rect.offsetLeft.toDouble()
                    currentY = e.clientY - rect.offsetTop + rect.scrollTop

                    val minX = min(startX, currentX)
                    val minY = min(startY, currentY)
                    val maxX = max(startX, currentX)
                    val maxY = max(startY, currentY)

                    val newList = mutableListOf<String>()

                    uiState.folderContent.forEach { element ->
                        val el = document.getElementById(element.uuid)?.unsafeCast<HTMLElement>() ?: return@forEach
                        val top = el.offsetTop
                        val left = el.offsetLeft
                        val bottom = top + el.offsetHeight
                        val right = left + el.offsetWidth

                        if (left < maxX && right > minX && top < maxY && bottom > minY) {
                            newList.add(element.uuid)
                        }
                    }

                    selectedElements = newList
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
        currentElements.forEach {
            if (it.type != Constants.FAKE)
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
                    onDeleteClick = {
                        if (it.parentId != Constants.TRASH)
                            viewModel.moveElementToFolder(it.uuid, Constants.TRASH, confirmed = true, onlyOne = true)
                        else viewModel.updateDeleteElements((uiState.selectedElements + it.uuid).distinct())
                    },
                    onDragStart = { e ->
                        draggingElement = it.uuid
                        e.dataTransfer?.setData("uuid", it.uuid)
                        e.dataTransfer?.effectAllowed = "move"
                    },
                    onDragOver = onDragOver@{ e ->
                        e.preventDefault()

                        val rect =
                            e.currentTarget?.unsafeCast<HTMLElement>()?.getBoundingClientRect() ?: return@onDragOver
                        val x = e.clientX - rect.left
                        val ratio = x / rect.width

                        dragZone = when {
                            ratio < (if (it.type == Constants.FOLDER) 0.20 else 0.50) -> DragZone.BEFORE
                            ratio > (if (it.type == Constants.FOLDER) 0.80 else 0.50) -> DragZone.AFTER
                            else -> DragZone.INSIDE
                        }

                        currentElements =
                            currentElements.addPlaceholder(uiState.selectedElements, draggingElement, it.uuid, dragZone)

                        e.dataTransfer?.dropEffect = "move"
                    },
                    onDrop = onDrop@{ e ->
                        val draggedId = e.dataTransfer?.getData("uuid") ?: return@onDrop
                        if (draggedId == it.uuid) return@onDrop

                        when (dragZone) {
                            DragZone.BEFORE -> viewModel.reindexElements(draggedId, it.uuid, true)
                            DragZone.AFTER -> viewModel.reindexElements(draggedId, it.uuid, false)
                            DragZone.INSIDE -> viewModel.moveElementToFolder(draggedId, it.uuid)
                            null -> {}
                        }

                        dragZone = null
                        draggingElement = ""
                    },
                    onRestoreClick = { viewModel.moveElementToFolder(it.uuid, it.undoTrash) },
                    parent = it.parentId,
                )
            else
                Card(
                    cardId = Constants.FAKE,
                    modifier = {
                        width(itemsSize.px)
                        height(itemsSize.px)
                    },
                    onEnter = {},
                    onDragStart = {},
                    onDragOver = { e -> e.preventDefault() },
                    onDrop = onDrop@{ e ->
                        val draggedId = e.dataTransfer?.getData("uuid") ?: return@onDrop
                        if (draggedId == it.uuid) return@onDrop

                        val fakeIndex = currentElements.indexOf(it)

                        when {
                            fakeIndex <= 0 ->
                                viewModel.reindexElements(draggedId, currentElements[1].uuid, true)
                            fakeIndex >= currentElements.lastIndex ->
                                viewModel.reindexElements(draggedId, currentElements[fakeIndex - 1].uuid, false)
                            else ->
                                viewModel.reindexElements(draggedId, currentElements[fakeIndex - 1].uuid, false)
                        }

                        dragZone = null
                        draggingElement = ""
                    },
                    content = {},
                )
        }
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