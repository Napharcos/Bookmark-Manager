package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.AppScope
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString
import org.napharcos.bookmarkmanager.options.OptionsViewModel
import org.napharcos.bookmarkmanager.options.UiState

@Composable
fun ChangeBackgroundDialog(
    uiState: UiState,
    viewModel: OptionsViewModel,
    onClose: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var image by remember { mutableStateOf(uiState.background) }

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
                    padding(4.px)
                    width(450.px)
                    height(410.px)
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
                    Text(getString(Values.CHANGE_BACKGROUND))
                }
                Div(
                    attrs = {
                        style {
                            alignSelf(AlignSelf.Center)
                            width(400.px)
                            height(225.px)
                            backgroundImage("url('$image')")
                            backgroundSize("cover")
                            backgroundPosition("center")
                            backgroundRepeat("no-repeat")
                        }
                    }
                )
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Row)
                            alignItems(AlignItems.FlexStart)
                            paddingBottom(4.px)
                            paddingRight(4.px)
                            paddingLeft(4.px)
                            paddingTop(8.px)
                        }
                    }
                ) {
                    Div(
                        attrs = {
                            style {
                                flexGrow(1)
                                paddingRight(4.px)
                                fontSize(1.1.em)
                            }
                        }
                    ) {
                        Text(getString(Values.BACKGROUND_IMAGE_URL))
                    }
                    Input(
                        type = InputType.Text,
                        attrs = {
                            style {
                                flexGrow(1)
                                paddingLeft(4.px)
                            }
                            value(if (image.startsWith("data:image")) "" else image)
                            onInput { image = it.value }
                        }
                    )
                }
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
                            onClick {
                                if (image.startsWith("data:image"))
                                    image = ""
                                else AppScope.scope.launch {
                                    image = viewModel.onBrowseImageClick()
                                }
                            }
                        }
                    ) {
                        Text(getString(if (image.startsWith("data:image")) Values.REMOVE else Values.BROWSE))
                    }
                }
            }
            ConfirmDialogButtons(
                onConfirm = { onConfirm(image) },
                onCancel = onClose
            )
        }
    }
}