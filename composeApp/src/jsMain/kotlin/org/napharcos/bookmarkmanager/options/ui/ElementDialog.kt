package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.data.Constants
import org.napharcos.bookmarkmanager.data.Values

@Composable
fun NewElementDialog(
    viewModel: ViewModel,
    onCancel: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var type by remember { mutableStateOf(Constants.FOLDER) }
    var image by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    ElementDialog(
        title = getString(Values.ADD_NEW_ELEMENT),
        type = type,
        typeChange = { type = it },
        typeEnabled = true,
        image = image,
        imageChange = { image = it },
        browseImage = {
            if (image.startsWith("data:image"))
                image = ""
            else
                AppScope.scope.launch { image = viewModel.onBrowseImageClick() }
        },
        name = name,
        nameChange = { name = it },
        url = url,
        urlChange = { url = it },
        confirmEnabled = if (type == Constants.FOLDER) name.isNotEmpty() else url.isNotEmpty(),
        onCancel = onCancel,
        onConfirm = { onConfirm(type, name, url, image) }
    )
}

@Composable
fun EditElementDialog(
    viewModel: ViewModel,
    bookmark: Bookmarks,
    onCancel: () -> Unit,
    onConfirm: (Bookmarks, String, String, String) -> Unit
) {
    var image by remember { mutableStateOf(bookmark.image) }
    var url by remember { mutableStateOf(bookmark.url) }
    var name by remember { mutableStateOf(bookmark.name) }

    ElementDialog(
        title = getString(Values.EDIT_ELEMENT),
        type = bookmark.type,
        typeChange = { _ ->  },
        typeEnabled = false,
        image = image,
        imageChange = { image = it },
        browseImage = {
            if (image.startsWith("data:image"))
                image = ""
            else
                AppScope.scope.launch { image = viewModel.onBrowseImageClick() }
        },
        name = name,
        nameChange = { name = it },
        url = url,
        urlChange = { url = it },
        confirmEnabled = true,
        onCancel = onCancel,
        onConfirm = { onConfirm(bookmark, name, url, image) }
    )
}

@Composable
fun ElementDialog(
    title: String,
    type: String,
    typeChange: (String) -> Unit,
    typeEnabled: Boolean,
    image: String,
    imageChange: (String) -> Unit,
    browseImage: () -> Unit,
    name: String,
    nameChange: (String) -> Unit,
    url: String,
    urlChange: (String) -> Unit,
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Div(
        attrs = {
            style { dialogBackground() }
            onClick { onCancel() }
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
                    width(450.px)
                    height(500.px)
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
                            fontSize(1.1.em)
                            textAlign("center")
                        }
                    }
                ) {
                    Text(title)
                }
                Div(
                    attrs = {
                        style {
                            alignSelf(AlignSelf.Center)
                            width(340.px)
                            height(221.px)
                            backgroundImage("url('$image')")
                            backgroundSize(if (image == "./folder.svg") "contain" else "scale-down")
                            backgroundPosition("center")
                            backgroundRepeat("no-repeat")
                        }
                    }
                )
                SelectElement(
                    text = getString(Values.TYPE),
                    onChange = typeChange,
                    enabled = typeEnabled,
                    selected = type
                )
                InputElement(
                    text = getString(Values.NAME),
                    value = name,
                    onValueChange = nameChange
                )
                if (type == Constants.URL)
                    InputElement(
                        text = getString(Values.PATH),
                        value = url,
                        onValueChange = urlChange
                    )
                InputElement(
                    text = getString(Values.BACKGROUND_IMAGE_URL),
                    value = if (image.startsWith("data:image")) "" else image,
                    onValueChange = imageChange
                )
                Div(
                    attrs = {
                        style {
                            alignItems(AlignItems.Center)
                        }
                    }
                ) {
                    Div(
                        attrs = {
                            style {
                                padding(4.px)
                                fontSize(1.1.em)
                            }
                        }
                    ) {
                        Text(getString(Values.OR))
                    }
                    Button(
                        attrs = {
                            style {
                                width(120.px)
                                marginTop(8.px)
                            }
                            onClick { browseImage() }
                        }
                    ) {
                        Text(getString(if (image.startsWith("data:image")) Values.REMOVE else Values.BROWSE))
                    }
                }
            }
            ConfirmDialogButtons(
                onConfirm = onConfirm,
                onCancel = onCancel,
                enabled = confirmEnabled
            )
        }
    }
}

@Composable
fun InputElement(
    text: String,
    value: String,
    onValueChange: (String) -> Unit
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
                value(value)
                onInput { onValueChange(it.value) }
            }
        )
    }
}

@Composable
fun SelectElement(
    text: String,
    enabled: Boolean,
    selected: String,
    onChange: (String) -> Unit
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
        Select(
            attrs = {
                if (!enabled) disabled()
                attr("value", selected)
                onChange { onChange(it.value ?: "") }
            }
        ) {
            Option(Constants.FOLDER) {
                Text(getString(Values.FOLDER))
            }
            Option(Constants.URL) {
                Text(getString(Values.BOOKMARK))
            }
        }
    }
}

fun StyleScope.elementStyle(): StyleScope {
    return this.apply {
        display(DisplayStyle.Grid)
        gridTemplateColumns("1fr 2fr")
        gap(8.px)
        paddingBottom(4.px)
        paddingRight(4.px)
        paddingLeft(4.px)
        paddingTop(8.px)
    }
}