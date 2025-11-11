package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticDragEvent
import org.jetbrains.compose.web.attributes.Draggable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Constants
import kotlin.js.Date
import kotlin.math.roundToInt

@Composable
fun FolderCardElement(
    uuid: String,
    parent: String,
    image: String,
    name: String,
    url: String,
    modified: Long,
    type: String,
    selected: Boolean,
    size: Int,
    onClick: () -> Unit,
    onDragStart: (SyntheticDragEvent) -> Unit,
    onDragOver: (SyntheticDragEvent) -> Unit,
    onDrop: (SyntheticDragEvent) -> Unit,
    onEditClick: () -> Unit,
    onSelectClick: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }

    var restoreEntered by remember { mutableStateOf(false) }

    Card(
        cardId = uuid,
        modifier = {
            width(size.px)
            height(size.px)
        },
        onEnter = { entered = it },
        onDragStart = onDragStart,
        onDragOver = onDragOver,
        onDrop = onDrop,
    ) {
        Div(
            attrs = {
                style {
                    position(Position.Absolute)
                    width(size.px)
                    height(size.px)
                    left(0.px)
                    top(0.px)
                    property("pointer-events", "none")
                }
            }
        ) {
            CardBaseContent(
                image = image,
                name = name,
                url = url,
                modified = modified,
                type = type,
                size = size,
                onClick = onClick
            )
        }
        if (entered || selected) {
            Div(
                attrs = {
                    style {
                        position(Position.Absolute)
                        width(size.px)
                        left(0.px)
                        top(0.px)
                        property("pointer-events", "none")
                    }
                }
            ) {
                CardIconsContent(
                    selected = selected,
                    entered = entered,
                    onDeleteClick = onDeleteClick,
                    onEditClick = onEditClick,
                    onSelectClick = onSelectClick,
                    size = size
                )
            }
            if (parent == Constants.TRASH) {
                Div(
                    attrs = {
                        style {
                            position(Position.Absolute)
                            width(size.px)
                            height(size.px)
                            left(0.px)
                            top(0.px)
                            property("pointer-events", "none")
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.Center)
                            alignItems(AlignItems.Center)
                        }
                    }
                ) {
                    Div(attrs = {
                        style {
                            width(50.px)
                            height(50.px)
                            borderRadius(50.percent)
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.Center)
                            alignItems(AlignItems.Center)
                            cursor("pointer")
                            property("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                            backgroundColor(if (restoreEntered) rgb(41, 182, 246) else rgb(230, 230, 230))
                            padding(4.px)
                            property("object-fit", "contain")
                            property("pointer-events", "auto")
                        }
                        onClick { onRestoreClick() }
                        onMouseEnter { restoreEntered = true }
                        onMouseLeave { restoreEntered = false }
                    }) {
                        Img("./refresh.svg")
                    }
                }
            }
        }
    }
}

@Composable
fun CardIconsContent(
    entered: Boolean,
    selected: Boolean,
    size: Int,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSelectClick: (Boolean) -> Unit
) {
    var selectEntered by remember { mutableStateOf(false) }
    var editEntered by remember { mutableStateOf(false) }
    var closeEntered by remember { mutableStateOf(false) }

    Div(
        attrs = { style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            justifyContent(JustifyContent.SpaceBetween)
            margin(8.px)
            property("pointer-events", "none")
        } }
    ) {
        if (selected || entered) {
            Div(attrs = {
                style {
                    iconButtonStyle(selectEntered, selected)
                    property("pointer-events", "auto")
                }
                onClick { onSelectClick(!selected) }
                onMouseEnter { selectEntered = true }
                onMouseLeave { selectEntered = false }
            }) {
                Img("./check.svg")
            }
        }
        Div(
            attrs = { style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                justifyContent(JustifyContent.FlexEnd)
                property("pointer-events", "none")
            } }
        ) {
            if (entered && size >= 150) {
                Div(attrs = {
                    style {
                        iconButtonStyle(editEntered, false)
                        marginRight(8.px)
                        property("pointer-events", "auto")
                    }
                    onClick { onEditClick() }
                    onMouseEnter { editEntered = true }
                    onMouseLeave { editEntered = false }
                }) {
                    Img("./edit.svg")
                }
            }
            if (entered) {
                Div(attrs = {
                    style {
                        iconButtonStyle(closeEntered, false)
                        property("pointer-events", "auto")
                    }
                    onClick { onDeleteClick() }
                    onMouseEnter { closeEntered = true }
                    onMouseLeave { closeEntered = false }
                }) {
                    Img("./close.svg")
                }
            }
        }
    }
}

@Composable
fun CardBaseContent(
    image: String,
    name: String,
    url: String,
    modified: Long,
    type: String,
    size: Int,
    onClick: () -> Unit
) {
    val contentWidth = size - 16
    val infoHeight = ((size - 8) * 0.35).coerceIn(60.0, 150.0).roundToInt()
    val imageHeight = (size - 8 - infoHeight)
    val nameHeight = infoHeight.calculateNameHeight(type)
    val urlHeight = infoHeight.calculateUrlHeight()

    Div(
        attrs = {
            style {
                width(contentWidth.px)
                height((size - 16).px)
                margin(8.px)
                property("pointer-events", "auto")
            }
            onClick { onClick() }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    width(contentWidth.px)
                    height(imageHeight.px)
                    backgroundColor(Color.transparent)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    property("pointer-events", "none")
                }
            }
        ) {
            Img(image) {
                style {
                    width(contentWidth.px)
                    height(imageHeight.px)
                    property("pointer-events", "none")
                    if (image == "./folder.svg")
                        property("object-fit", "contain")
                    else property("object-fit", "scale-down")
                }
            }
        }

        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    width(contentWidth.px)
                    height(nameHeight.px)
                    backgroundColor(Color.transparent)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    property("pointer-events", "none")
                }
            }
        ) {
            Div(
                attrs = {
                    style {
                        width(contentWidth.px)
                        height(nameHeight.px)
                        backgroundColor(Color.transparent)
                        justifyContent(JustifyContent.Center)
                        alignItems(AlignItems.Center)
                        maxHeight(2.8.em)
                        lineHeight(1.4.em)
                        textAlign("center")
                        fontSize(1.2.em)
                        if (infoHeight >= 75 || type == Constants.FOLDER) {
                            property("display", "-webkit-box")
                            property("-webkit-box-orient", "vertical")
                            property("-webkit-line-clamp", 2)
                        } else {
                            whiteSpace("nowrap")
                        }
                        property("text-overflow", "ellipsis")
                        overflow("hidden")
                        cursor("default")
                        property("pointer-events", "none")
                    }
                }
            ) {
                Text(name)
            }
        }

        if (type != Constants.FOLDER) {
            Div(
                attrs = {
                    style {
                        width(contentWidth.px)
                        height(urlHeight.px)
                        backgroundColor(Color.transparent)
                        justifyContent(JustifyContent.Center)
                        alignItems(AlignItems.FlexStart)
                        fontSize(1.1.em)
                        property("text-overflow", "ellipsis")
                        whiteSpace("nowrap")
                        overflow("hidden")
                        cursor("default")
                        property("pointer-events", "none")
                    }
                }
            ) {
                Text(url.substringAfter("//").substringBefore("/"))
            }
        }

        if (size >= 200) {
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.FlexEnd)
                        alignItems(AlignItems.Center)
                        width(contentWidth.px)
                        height(if (type == Constants.FOLDER) (infoHeight - nameHeight).px else (infoHeight - nameHeight - urlHeight).px)
                        backgroundColor(Color.transparent)
                        fontSize(0.9.em)
                        cursor("default")
                        property("pointer-events", "none")
                    }
                }
            ) {
                Text(Date(modified).toLocaleString())
            }
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
fun Card(
    cardId: String,
    modifier: StyleScope.() -> Unit = {},
    onEnter: (Boolean) -> Unit = { _ -> },
    onDragStart: (SyntheticDragEvent) -> Unit,
    onDragOver: (SyntheticDragEvent) -> Unit,
    onDrop: (SyntheticDragEvent) -> Unit,
    content: @Composable () -> Unit
) {
    var entered by remember { mutableStateOf(false) }

    Div(
        attrs = {
            id(cardId)
            draggable(Draggable.True)
            style {
                position(Position.Relative)
                borderRadius(12.px)
                property("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                backgroundColor(if (entered) rgba(60, 60, 60, 0.6) else rgba(45, 45, 45, 0.6))
                property("user-select", "none")
                property("pointer-events", "auto")
                modifier()
            }
            onMouseEnter { entered = true; onEnter(entered) }
            onMouseLeave { entered = false; onEnter(entered) }
            onDragStart { onDragStart(it) }
            onDragOver { onDragOver(it) }
            onDrop { onDrop(it) }
        }
    ) {
        content()
    }
}

fun StyleScope.iconButtonStyle(entered: Boolean, selected: Boolean): StyleScope {
    return this.apply {
        width(25.px)
        height(25.px)
        borderRadius(50.percent)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
        cursor("pointer")
        property("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
        backgroundColor(if (entered) rgb(41, 182, 246) else if (selected) rgb(2, 119, 189) else rgb(230, 230, 230))
        padding(4.px)
        property("object-fit", "contain")
    }
}

fun Int.calculateNameHeight(type: String): Int {
    val height =  if (type == Constants.FOLDER) {
        if (this < 67) this * 1.0 else this * 0.75
    } else this * 0.5
    return height.roundToInt()
}

fun Int.calculateUrlHeight(): Int {
    val height = if (this < 67) this * 0.5 else this * 0.25
    return height.roundToInt()
}