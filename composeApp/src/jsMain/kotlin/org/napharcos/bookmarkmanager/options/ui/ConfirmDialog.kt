package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString

@Composable
fun ConfirmDialog(
    title: String = "Cím",
    text: String = "Teszt szöveg.",
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
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
                    minWidth(300.px)
                    minHeight(150.px)
                    maxHeight(300.px)
                    textAlign("center")
                    property("box-shadow", "rgba(0, 0, 0, 0.3) 0px 4px 12px")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            Div {
                H3(
                    attrs = {
                        style {
                            textAlign("center")
                        }
                    }
                ) {
                    Text(title)
                }
                Div(
                    attrs = {
                        style {
                            fontSize(1.2.em)
                        }
                    }
                ) {
                    Text(text)
                }
            }
            ConfirmDialogButtons(
                onConfirm = onConfirm,
                onCancel = onClose
            )
        }
    }
}

@Composable
fun ConfirmDialogButtons(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    enabled: Boolean = true
) {
    var confirmEntered by remember { mutableStateOf(false) }
    var cancelEntered by remember { mutableStateOf(false) }

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
            Text(getString(Values.CANCEL))
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
            Text(getString(Values.CONFIRM))
        }
    }
}

fun StyleScope.dialogButton(entered: Boolean, withMargin: Boolean = true, enabled: Boolean = true): StyleScope {
    return this.apply {
        marginTop(8.px)
        if (withMargin) marginRight(8.px)
        if (withMargin) marginLeft(8.px)
        height(34.px)
        backgroundColor(if (entered && enabled) rgb(80, 80, 80) else Color.transparent)
        if (enabled) cursor("pointer")
        overflow("hidden")
        borderRadius(17.px,)
        border(1.px, LineStyle.Solid, Color.transparent)
        property("border", "none")
        property("user-select", "none")
        color(if (enabled) Color.lightblue else Color.gray)
    }
}