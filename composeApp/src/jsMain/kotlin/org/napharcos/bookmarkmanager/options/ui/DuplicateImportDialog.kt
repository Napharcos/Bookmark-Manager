package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.database.DatabaseRepository
import kotlin.js.Date

@Composable
fun DuplicateImportDialog(
    newElement: BookmarkData,
    newParentId: String,
    database: DatabaseRepository,
    title: String,
    cancelText: String,
    confirmText: String,
    thirdText: String,
    enableThird: Boolean,
    onCancel: (Boolean) -> Unit,
    onConfirm: (Boolean) -> Unit,
    onThird: (Boolean) -> Unit
) {
    var oldElement by remember { mutableStateOf<Bookmarks?>(null) }

    var newParentName by remember { mutableStateOf("") }
    var oldParentName by remember { mutableStateOf("") }

    LaunchedEffect(newElement, Unit) {
        oldElement = database.getBookmark(this, newElement.guid) ?: database.getBookmarkByUrl(this, newElement.url)
        oldElement?.parentId?.let { oldParentName = database.getBookmark(this, it)?.name ?: "" }
        if (newParentId.isNotEmpty())
            newParentName = database.getBookmark(this, newParentId)?.name ?: ""
    }

    val newDate =
        (if (newElement.date_modified.isNotEmpty() && newElement.date_modified != "0") newElement.date_modified else newElement.date_added)
            .toLongOrNull()?.convertChromeTime() ?: 0L

    Div(
        attrs = {
            style { dialogBackground() }
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
                    padding(4.px)
                    width(800.px)
                    height(550.px)
                    textAlign("center")
                    property("box-shadow", "rgba(0, 0, 0, 0.3) 0px 4px 12px")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    justifyContent(JustifyContent.Center)
                    alignContent(AlignContent.Center)
                }
            }) {
                H3({
                    style {
                        height(25.px)
                        fontSize(1.3.em)
                        textAlign("center")
                    }
                }) {
                    Text(title)
                }
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Row)
                    }
                }) {
                    Div({
                        style {
                            flexGrow(1)
                            flexBasis(0.px)
                        }
                    }) {
                        DuplicateContent(
                            image = oldElement?.image ?: "",
                            title = getString(Values.OLD_ELEMENT),
                            id = oldElement?.uuid ?: "",
                            type = oldElement?.type ?: "",
                            name = oldElement?.name ?: "",
                            parent = oldParentName,
                            url = oldElement?.url ?: "",
                            modified = oldElement?.modified?.toLong() ?: 0L
                        )
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexGrow(0.1)
                            justifyContent(JustifyContent.Center)
                            alignItems(AlignItems.Center)
                        }
                    }) {
                        SplitVerticalLine()
                    }
                    Div({
                        style {
                            flexGrow(1)
                            flexBasis(0.px)
                        }
                    }) {
                        DuplicateContent(
                            image = "",
                            title = getString(Values.NEW_ELEMENT),
                            id = newElement.guid,
                            type = newElement.type,
                            name = newElement.name,
                            parent = newParentName,
                            url = newElement.url,
                            modified = newDate
                        )
                    }
                }
            }
            DuplicateBottomButtons(
                cancelText = cancelText,
                confirmText = confirmText,
                thirdText = thirdText,
                onCancel = onCancel,
                onThird = onThird,
                onConfirm = onConfirm,
                applyForAllText = getString(Values.APPLY_FOR_ALL),
                enableThird = enableThird
            )
        }
    }
}

@Composable
fun DuplicateContent(
    image: String,
    title: String,
    id: String,
    type: String,
    name: String,
    parent: String,
    url: String,
    modified: Long,
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
                    margin(0.px)
                    height(20.px)
                    fontSize(1.1.em)
                    textAlign("center")
                }
            }
        ) {
            Text(title)
        }
        Div({
            style {
                alignSelf(AlignSelf.Center)
                width(340.px)
                height(221.px)
                backgroundImage("url('$image')")
                backgroundSize(if (image == "./folder.svg") "contain" else "scale-down")
                backgroundPosition("center")
                backgroundRepeat("no-repeat")
            }
        })
        DuplicateInput(
            text = getString(Values.IDENTIFIER),
            value = id,
        )
        SelectElement(
            text = getString(Values.TYPE),
            onChange = {},
            enabled = false,
            selected = type
        )
        DuplicateInput(
            text = getString(Values.NAME),
            value = name,
        )
        DuplicateInput(
            text = getString(Values.PARENT),
            value = parent,
        )
        if (type == Constants.URL)
            DuplicateInput(
                text = getString(Values.PATH),
                value = url,
            )
        DuplicateInput(
            text = getString(Values.MODIFIED),
            value = Date(modified).toLocaleString()
        )
    }
}

@Composable
fun DuplicateBottomButtons(
    cancelText: String,
    confirmText: String,
    thirdText: String,
    onCancel: (Boolean) -> Unit,
    onThird: (Boolean) -> Unit,
    onConfirm: (Boolean) -> Unit,
    applyForAllText: String,
    enableThird: Boolean
) {
    var applyForAll by remember { mutableStateOf(false) }

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.FlexEnd)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                justifyContent(JustifyContent.Start)
                paddingBottom(4.px)
            }
        }) {
            Input(
                type = InputType.Checkbox,
                attrs = {
                    checked(applyForAll)
                    onChange { applyForAll = it.value }
                }
            )
            Div({
                style {
                    paddingLeft(6.px)
                    cursor("default")
                }
                onClick { applyForAll = !applyForAll }
            }) {
                Text(applyForAllText)
            }
        }
        DuplicateDialogButtons(
            onConfirm = { onConfirm(applyForAll) },
            onCancel = { onCancel(applyForAll) },
            closeText = cancelText,
            confirmText = confirmText,
            onThird = { onThird(applyForAll) },
            thirdText = thirdText,
            third = enableThird
        )
    }
}

@Composable
fun DuplicateInput(
    text: String,
    value: String,
) {
    Div(attrs = { style { elementStyle() } }) {
        Div(
            attrs = {
                style {
                    fontSize(1.1.em)
                    textAlign("left")
                }
            }
        ) {
            Text(text)
        }
        Input(
            type = InputType.Text,
            attrs = {
                style {
                    backgroundColor(rgb(197, 197, 197))
                    color(rgb(104, 104, 104))
                }
                value(value)
                onInput {
                    it.preventDefault()
                    it.stopPropagation()
                }
            }
        )
    }
}

@Composable
fun DuplicateDialogButtons(
    closeText: String = getString(Values.CANCEL),
    confirmText: String = getString(Values.CONFIRM),
    thirdText: String = "",
    onConfirm: () -> Unit,
    onThird: () -> Unit,
    onCancel: () -> Unit,
    third: Boolean = true,
    enabled: Boolean = true
) {
    var confirmEntered by remember { mutableStateOf(false) }
    var cancelEntered by remember { mutableStateOf(false) }
    var thirdEntered by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                justifyContent(JustifyContent.FlexEnd)
                alignItems(AlignItems.FlexEnd)
            }
        }
    ) {
        Button(
            attrs = {
                onClick { onCancel() }
                style { dialogButton(cancelEntered) }
                onMouseEnter { cancelEntered = true }
                onMouseLeave { cancelEntered = false }
            }
        ) {
            Text(closeText)
        }
        if (third)
            Button(
                attrs = {
                    onClick { onThird() }
                    style { dialogButton(thirdEntered) }
                    onMouseEnter { thirdEntered = true }
                    onMouseLeave { thirdEntered = false }
                }
            ) {
                Text(thirdText)
            }
        Button(
            attrs = {
                if (!enabled) disabled()
                onClick { onConfirm() }
                style { dialogButton(confirmEntered, enabled = enabled) }
                onMouseEnter { confirmEntered = true }
                onMouseLeave { confirmEntered = false }
            }
        ) {
            Text(confirmText)
        }
    }
}

@Composable
fun SplitVerticalLine() {
    Hr(
        attrs = {
            style {
                property("border", "none")
                margin(0.px)
                height(100.percent)
                width(1.px)
                backgroundColor(rgb(30, 30, 30))
            }
        }
    )
}