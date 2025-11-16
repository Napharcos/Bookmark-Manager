package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.napharcos.bookmarkmanager.data.Values
import org.napharcos.bookmarkmanager.getString

@Composable
fun DeleteDBDialog(
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    val acceptText by remember { mutableStateOf(getString(Values.DELETE_DB_CONFIRM_TEXT)) }

    var confirmEnabled by remember { mutableStateOf(false) }

    var input by remember { mutableStateOf("") }

    LaunchedEffect(input) {
        confirmEnabled = input == acceptText
    }

    ConfirmDialog(
        title = getString(Values.DELETE_DB),
        text = getString(Values.DELETE_DB_DIALOG_TEXT, acceptText),
        onClose = onClose,
        onConfirm = onConfirm,
        enabled = confirmEnabled
    ) {
        Input(
            type = InputType.Text,
            attrs = {
                style {
                    marginTop(8.px)
                }
                value(input)
                onInput { input = it.value }
            }
        )
    }
}