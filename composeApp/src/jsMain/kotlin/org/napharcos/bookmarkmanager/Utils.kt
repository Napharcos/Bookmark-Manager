package org.napharcos.bookmarkmanager

import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.css.whiteSpace
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.data.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun folderNameBuilder(folderDepth: Int, folderName: String, open: Boolean, hasChild: Boolean): String {
    val indent = " ".repeat(folderDepth)
    val icon = if (!hasChild) "  " else if (open) "\u23F7" else "\u23F5"
    return "$indent$icon $folderName"
}

@Composable
fun SplitLine(topMargin: Boolean = false) {
    Hr(
        attrs = {
            style {
                property("border", "none")
                margin(0.px)
                if (topMargin) marginTop(4.px)
                height(1.px)
                width(100.percent)
                backgroundColor(rgb(59, 59, 59))
            }
        }
    )
}

@Composable
fun PageName(title: String) {
    Div(
        attrs = {
            style {
                paddingLeft(12.px)
                paddingRight(12.px)
                overflow("hidden")
                property("display", "-webkit-box")
                property("-webkit-box-orient", "vertical")
                property("-webkit-line-clamp", 2)
                property("text-overflow", "ellipsis")
            }
        }
    ) {
        Text(title)
    }
}

fun StyleScope.elementDiv() {
    this.apply {
        property("display", "flex")
        justifyContent(JustifyContent.SpaceBetween)
        width(100.percent)
        height(30.px)
    }
}

fun StyleScope.elementButton(applyColor: Boolean = true) {
    this.apply {
        height(100.percent)
        backgroundColor(Color.transparent)
        cursor("pointer")
        overflow("hidden")
        property("border", "none")
        property("user-select", "none")
        property("display", "-webkit-box")
        property("-webkit-box-orient", "vertical")
        property("-webkit-line-clamp", 1)
        property("text-overflow", "ellipsis")
        property("text-align", "left")
        whiteSpace("pre")
        fontFamily("Fira Code", "monospace")
        fontSize(1.2.em)
        if (applyColor) color(Color.lightgray)
    }
}

fun StyleScope.dialogBackground() {
    position(Position.Fixed)
    top(0.px)
    left(0.px)
    width(100.vw)
    height(100.vh)
    backgroundColor(rgba(0, 0, 0, 0.4))
    display(DisplayStyle.Flex)
    justifyContent(JustifyContent.Center)
    alignItems(AlignItems.Center)
}

enum class DragZone { BEFORE, AFTER, INSIDE }

fun List<Bookmarks>.addPlaceholder(
    selectedElements: List<String>,
    element: String,
    target: String,
    dragZone: DragZone?
): List<Bookmarks> {
    val movingIds = (selectedElements + element).distinct()
    val list = this.filterNot { it.uuid in movingIds || it.type == Constants.FAKE }.toMutableList()
    val targetIndex = list.indexOfFirst { it.uuid == target }
    if (targetIndex == -1) return this

    val placeholder = Bookmark(
        uuid = Constants.FAKE,
        name = "",
        type = Constants.FAKE,
        index = -1,
        imageId = "",
        image = ""
    )

    val insertPos = if (dragZone == DragZone.BEFORE) targetIndex else (targetIndex + 1).coerceAtMost(list.size)
    list.add(insertPos, placeholder)
    return if (dragZone != DragZone.INSIDE) list else this.filter { it.type != Constants.FAKE }
}