package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.dialogBackground
import org.napharcos.bookmarkmanager.getString

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    closeText: String = getString(Values.CANCEL),
    confirmText: String = getString(Values.CONFIRM),
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    enabled: Boolean = true,
    moreContent: (@Composable () -> Unit)? = null
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
                    padding(8.px)
                    minWidth(300.px)
                    minHeight(150.px)
                    maxWidth(450.px)
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
                            if (moreContent != null) textAlign("left")
                            fontSize(1.2.em)
                        }
                    }
                ) {
                    Text(text)
                }
                if (moreContent != null)
                    moreContent()
            }
            ConfirmDialogButtons(
                onConfirm = onConfirm,
                onCancel = onClose,
                closeText = closeText,
                confirmText = confirmText,
                enabled = enabled
            )
        }
    }
}

@Composable
fun ConfirmDialogButtons(
    closeText: String = getString(Values.CANCEL),
    confirmText: String = getString(Values.CONFIRM),
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
            Text(closeText)
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