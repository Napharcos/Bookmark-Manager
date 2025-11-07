package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
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
    image: String,
    name: String,
    url: String,
    modified: Long,
    type: String,
    selected: Boolean,
    size: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onSelectClick: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }

    Card(
        cardId = uuid,
        modifier = {
            width(size.px)
            height(size.px)
        },
        onEnter = { entered = it }
    ) {
        Div(
            attrs = {
                style {
                    position(Position.Absolute)
                    width(size.px)
                    height(size.px)
                    left(0.px)
                    top(0.px)
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
        } }
    ) {
        if (selected || entered){
            Div(attrs = {
                style { iconButtonStyle(selectEntered, selected) }
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
            } }
        ) {
            if (entered && size >= 150) {
                Div(attrs = {
                    style {
                        iconButtonStyle(editEntered, false)
                        marginRight(8.px)
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
                    style { iconButtonStyle(closeEntered, false) }
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
                }
            }
        ) {
            Img(image) {
                style {
                    width(contentWidth.px)
                    height(imageHeight.px)
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
                    }
                }
            ) {
                Text(Date(modified).toLocaleString())
            }
        }
    }
}

@Composable
fun Card(
    cardId: String,
    modifier: StyleScope.() -> Unit = {},
    onEnter: (Boolean) -> Unit = { _ -> },
    content: @Composable () -> Unit
) {
    var entered by remember { mutableStateOf(false) }

    Div(
        attrs = {
            id(cardId)
            style {
                position(Position.Relative)
                borderRadius(12.px)
                property("box-shadow", "0 4px 12px rgba(0,0,0,0.2)")
                backgroundColor(if (entered) rgba(60, 60, 60, 0.6) else rgba(45, 45, 45, 0.6))
                property("user-select", "none")
                modifier()
            }
            onMouseEnter { entered = true; onEnter(entered) }
            onMouseLeave { entered = false; onEnter(entered) }
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
    } else {
        this * 0.5
    }
    return height.roundToInt()
}

fun Int.calculateUrlHeight(): Int {
    val height = if (this < 67) this * 0.5 else this * 0.25
    return height.roundToInt()
}