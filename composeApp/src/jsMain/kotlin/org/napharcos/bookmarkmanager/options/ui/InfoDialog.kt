package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.css.AlignContent
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignContent
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Iframe
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString

@Composable
fun InfoDialog(
    title: String,
    text: String,
    file: String?,
    height: Int = 250,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    var cancelEntered by remember { mutableStateOf(false) }

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
                    padding(8.px)
                    width(450.px)
                    height(height.px)
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
                    Text(getString(title))
                }
                Div(
                    attrs = {
                        style {
                            width(100.percent)
                            if (file == null) padding(4.px)
                            fontSize(1.1.em)
                            textAlign("center")
                        }
                    }
                ) {
                    content?.invoke() ?: file?.let {
                        Iframe(attrs = {
                            style {
                                width(425.px)
                                fontSize(1.2.em)
                                height(152.px)
                            }
                            attr("src", file)
                        })
                    } ?: Text(getString(text))
                }
            }
            Button(
                attrs = {
                    onClick { onConfirm() }
                    style {
                        width(100.percent)
                        dialogButton(cancelEntered, false)
                    }
                    onMouseEnter { cancelEntered = true }
                    onMouseLeave { cancelEntered = false }
                }
            ) {
                Text(getString(Values.OK))
            }
        }
    }
}