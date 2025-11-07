package org.napharcos.bookmarkmanager.popup

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.data.Values

@Composable
fun PopupSummary() {
    val viewModel = remember { PopupViewModel() }

    val title = remember { getString(Values.ADD_BOOKMARK) }
    val open = remember { getString(Values.OPEN_OPTIONS) }

    var pageData by remember { mutableStateOf<PageData?>(null) }

    LaunchedEffect(Unit) {
        val options = js("{}").unsafeCast<CaptureOptions>()
        options.format = "png"

        chrome.tabs.captureVisibleTab(null, options) { screenShot ->
            onPopupOpen { msg ->
                val message = msg.unsafeCast<dynamic>()

                if (message.type == "PAGE_INFO") {
                    val payload = message.payload.unsafeCast<dynamic>()

                    val info = PageInfo(
                        fullUrl = payload.fullUrl as? String ?: "",
                        baseDomain = payload.baseDomain as? String ?: "",
                        title = payload.title as? String ?: "",
                        favicons = (payload.favicons as? Array<dynamic>)?.mapNotNull { it as? String }?.toTypedArray()
                            ?: emptyArray(),
                        metaImages = (payload.metaImages as? Array<dynamic>)?.mapNotNull { it as? String }
                            ?.toTypedArray() ?: emptyArray(),
                        pageImages = (payload.pageImages as? Array<dynamic>)?.mapNotNull { it as? String }
                            ?.toTypedArray() ?: emptyArray()
                    )

                    pageData = info.toPageData(screenShot)
                }
            }
        }
    }

    Div(
        attrs = {
            style {
                overflow("hidden")
            }
        }
    ) {
        PopupTitle(title, open)
        ImageView(pageData?.images ?: emptyList())
        PageName(pageData?.title ?: "")
        SplitLine(true)
        BookmarkTree(viewModel)
        SplitLine()
        BottomButtons {  }
    }
}

@Composable
fun BookmarkTree(viewModel: PopupViewModel) {
    var selectedElement by remember { mutableStateOf("") }

    Div(
        attrs = {
            style {
                width(342.px)
                maxHeight(240.px)
                overflowY("auto")
                paddingRight(4.px)
                paddingLeft(4.px)
                property("scrollbar-color", "#555 #3b3b3b")
            }
        }
    ) {
        FoldersTree(
            viewModel = viewModel,
            list = viewModel.folders.map { it.value }.buildTree(),
            openFolders = viewModel.openFolders,
            depth = 0,
            selected = selectedElement,
            select = { selectedElement = it }
        )
    }
}

@Composable
fun FoldersTree(
    viewModel: PopupViewModel,
    openFolders: List<String>,
    list: List<BookmarksTree>,
    depth: Int,
    selected: String,
    select: (String) -> Unit
) {
    list.sortedBy { it.folder.index }.forEach {
        val isOpen = it.folder.uuid in openFolders

        BookmarkTreeElement(
            folderName = folderNameBuilder(depth, it.folder.name, isOpen, it.children.isNotEmpty()),
            selected = selected == it.folder.uuid,
            onElementClick = {
                select(it.folder.uuid)
                if (isOpen) viewModel.closeFolder(it.folder.uuid)
                else viewModel.openFolder(it.folder.uuid)
            },
            onNewFolderClick = {  }
        )

        if (isOpen) {
            FoldersTree(
                viewModel = viewModel,
                openFolders = openFolders,
                list = it.children,
                depth = depth + 1,
                selected = selected,
                select = select
            )
        }
    }
}

@Composable
fun BookmarkTreeElement(
    folderName: String,
    selected: Boolean,
    onElementClick: () -> Unit,
    onNewFolderClick: () -> Unit
) {
    var onEnter by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                elementDiv()
                backgroundColor(if (onEnter) Color.darkblue else if (selected) Color.midnightblue else Color.transparent)
            }
            onMouseEnter { onEnter = true }
            onMouseLeave { onEnter = false }
        }
    ) {
        Button(
            attrs = {
                style {
                    width(306.px)
                    elementButton()
                }
                onClick { onElementClick() }
            }
        ) {
            Text(folderName)
        }
        Button(
            attrs = {
                onClick {
                    onNewFolderClick()
                    onElementClick()
                }
                style {
                    width(30.px)
                    height(30.px)
                    property("border", "none")
                    property("user-select", "none")
                    padding(2.px)
                    backgroundColor(Color.transparent)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    cursor("pointer")
                }
            }
        ) {
            Img("new_folder.svg")
        }
    }
}

@Composable
fun ImageView(
    images: List<String>
) {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(images.size) {
        currentIndex = 0
    }

    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                marginTop(8.px)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                width(100.percent)
                height(100.percent)
                backgroundColor(Color.transparent)
            }
        }
    ) {
        Button(
            attrs = {
                style { arrowButton() }
                onClick { if (currentIndex != 0) currentIndex-- else currentIndex = images.size - 1 }
            }
        ) {
            Text("‹")
        }

        Div(
            attrs = {
                style {
                    width(300.px)
                    height(220.px)
                    property("display", "flex")
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    property("overflow", "hidden")
                    position(Position.Relative)
                }
            }
        ) {
            if (images.isNotEmpty())
                Img(src = images[currentIndex]) {
                    style {
                        maxWidth(100.percent)
                        maxHeight(100.percent)
                        property("object-fit", "contain")
                        property("display", "block")
                    }
                }
        }

        Button(
            attrs = {
                style { arrowButton() }
                onClick { if (currentIndex != images.size - 1) currentIndex++ else currentIndex = 0 }
            }
        ) {
            Text("›")
        }
    }
}

@Composable
fun BottomButtons(remove: () -> Unit) {
    val okButtonText = remember { getString(Values.OK) }
    var onTrashEnter by remember { mutableStateOf(false) }
    var onOkEnter by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                property("display", "flex")
                justifyContent(JustifyContent.SpaceBetween)
                paddingBottom(4.px)
                width(100.percent)
                height(40.px)
                backgroundColor(Color.transparent)
            }
        }
    ) {
        Button(
            attrs = {
                onClick {  }
                style {
                    marginTop(4.px)
                    marginLeft(8.px)
                    width(34.px)
                    height(34.px)
                    property("border", "none")
                    property("user-select", "none")
                    padding(2.px)
                    backgroundColor(if (onTrashEnter) rgb(110, 110, 110) else rgb(80, 80, 80))
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, Color.transparent)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    cursor("pointer")
                }
                onMouseEnter { onTrashEnter = true }
                onMouseLeave { onTrashEnter = false }
            }
        ) {
            Img("trash.svg")
        }
        Button(
            attrs = {
                onClick {  }
                style {
                    marginTop(4.px)
                    marginRight(8.px)
                    width(54.px)
                    height(34.px)
                    backgroundColor(if (onOkEnter) rgb(0, 0, 185) else rgb(0, 0, 140))
                    cursor("pointer")
                    overflow("hidden")
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, Color.transparent)
                    property("border", "none")
                    property("user-select", "none")
                    color(Color.lightgray)
                }
                onMouseEnter { onOkEnter = true }
                onMouseLeave { onOkEnter = false }
            }
        ) {
            Text(okButtonText)
        }
    }
}

@Composable
fun PopupTitle(
    title: String,
    open: String
) {
    Div {
        H2(
            attrs = {
                style {
                    paddingLeft(12.px)
                }
            }
        ) {
            Text(title)
        }
        A(
            href = "./options.html",
            attrs = {
                target(ATarget.Blank)
                style {
                    paddingLeft(12.px)
                    color(rgb(77, 163, 255))
                }
            }
        ) {
            Text(open)
        }
    }
}

fun StyleScope.arrowButton() {
    this.apply {
        background("transparent")
        property("border", "none")
        property("user-select", "none")
        width(40.px)
        height(220.px)
        fontSize(50.px)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
        cursor("pointer")
        color(Color.white)
    }
}