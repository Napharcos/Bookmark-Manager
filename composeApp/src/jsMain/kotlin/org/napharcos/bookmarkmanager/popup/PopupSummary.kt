package org.napharcos.bookmarkmanager.popup

import androidx.compose.runtime.*
import kotlinx.dom.isElement
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.napharcos.bookmarkmanager.*
import org.napharcos.bookmarkmanager.container.ContainerImpl
import org.napharcos.bookmarkmanager.data.Values
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

@Composable
fun PopupSummary() {
    val container = remember { ContainerImpl() }
    val viewModel = remember { ViewModel(container, true) }
    val uiState by viewModel.uiState.collectAsState()

    var imageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.getPageData()
    }

    LaunchedEffect(uiState.pageData?.images?.size) {
        imageIndex = 0
    }

    Div(
        attrs = {
            style {
                overflow("hidden")
            }
        }
    ) {
        PopupTitle()
        ImageView(
            images = uiState.pageData?.images ?: emptyList(),
            imageIndex = imageIndex,
            changeImageIndex = { imageIndex = it },
        )
        PageName(uiState.pageData?.title ?: "")
        SplitLine(true)
        BookmarkTree(uiState, viewModel)
        SplitLine()
        BottomButtons(
            remove = { viewModel.onPopupTrashClick() },
            confirm = { viewModel.onPopupOkClick(uiState.pageData?.images[imageIndex] ?: "") }
        )
    }
}

@Composable
fun BookmarkTree(
    uiState: UiState,
    viewModel: ViewModel
) {
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
            uiState = uiState,
            viewModel = viewModel,
            list = uiState.folders.buildTree(),
            openFolders = uiState.openFolders,
            depth = 0,
        )
    }
}

@Composable
fun FoldersTree(
    uiState: UiState,
    viewModel: ViewModel,
    openFolders: List<String>,
    list: List<BookmarksTree>,
    depth: Int,
) {
    val editable = list.firstOrNull { it.folder.uuid == uiState.newFolder }

    list.sortedBy { it.folder.index }.moveLastToFirst(editable != null).forEach {
        val isOpen = it.folder.uuid in openFolders

        BookmarkTreeElement(
            folderName = folderNameBuilder(depth, it.folder.name, isOpen, it.children.isNotEmpty()),
            selected = uiState.selectedFolder == it.folder.uuid,
            onElementClick = { viewModel.onNavElementClick(it.folder, it.folder.uuid) },
            onNewFolderClick = { viewModel.createNewPopupFolder(it.folder.uuid) },
            onFoldClick = { viewModel.onNavElementFoldClick(it.folder.uuid) },
            edit = uiState.newFolder == it.folder.uuid,
            onEditConfirm = { name -> viewModel.onPopupEditConfirm(it.folder, name) }
        )

        if (isOpen) {
            FoldersTree(
                uiState = uiState,
                viewModel = viewModel,
                openFolders = openFolders,
                list = it.children,
                depth = depth + 1,
            )
        }
    }
}

@Composable
fun BookmarkTreeElement(
    folderName: String,
    selected: Boolean,
    onElementClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onFoldClick: () -> Unit,
    onEditConfirm: (String) -> Unit,
    edit: Boolean
) {
    var onEnter by remember { mutableStateOf(false) }

    val regex = listOf("\u23F7", "\u23F5").find { folderName.contains(it) }
    val names = regex?.let { folderName.split(it) } ?: listOf(folderName)

    var onFoldEnter by remember { mutableStateOf(false) }

    var elementRef by remember { mutableStateOf<HTMLElement?>(null) }
    var inputRef by remember { mutableStateOf<HTMLInputElement?>(null) }

    var value by remember { mutableStateOf(folderName.trim()) }

    LaunchedEffect(inputRef, Unit) {
        if (edit)
            inputRef?.focus()
    }

    LaunchedEffect(folderName) {
        value = folderName
    }

    Div(
        attrs = {
            style {
                elementDiv()
                backgroundColor(if (onEnter) rgba(0, 0, 139, 0.9) else if (selected) rgba(25, 25, 112, 0.9) else Color.transparent)
            }
            onMouseEnter { onEnter = true }
            onMouseLeave { onEnter = false }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Row)
                    justifyContent(JustifyContent.Start)
                    height(30.px)
                    backgroundColor(Color.transparent)
                }
            }
        ) {
            if (names.size > 1) {
                Button(
                    attrs = {
                        ref {
                            elementRef = it
                            onDispose { elementRef = null }
                        }
                        style {
                            elementButton()
                            paddingRight(0.px)
                            if (onFoldEnter) color(Color.cyan)
                        }
                        onClick { onFoldClick() }
                        onMouseEnter {
                            onFoldEnter = true
                            onEnter = true
                        }
                        onMouseLeave {
                            onFoldEnter = false
                            onEnter = false
                        }
                    }
                ) {
                    Text(names[0] + regex)
                }
            }
            if (!edit) {
                Button(
                    attrs = {
                        style {
                            width((300 - (elementRef?.offsetWidth ?: 0)).px)
                            elementButton()
                        }
                        onClick { onElementClick() }
                    }
                ) {
                    Text(if (names.size > 1) names[1] else " ${names[0]}")
                }
            } else {
                Input(
                    type = InputType.Text,
                    attrs = {
                        ref {
                            inputRef = it
                            onDispose { inputRef = null }
                        }
                        style {
                            width((300 - 22 - (elementRef?.offsetWidth ?: 0)).px)
                            height(18.px)
                            marginTop(4.px)
                            marginBottom(4.px)
                            marginLeft(8.px)
                            marginRight(8.px)
                            padding(0.px)
                            overflow("hidden")
                            property("display", "-webkit-box")
                            property("-webkit-box-orient", "vertical")
                            property("-webkit-line-clamp", 1)
                            property("text-overflow", "ellipsis")
                            property("text-align", "left")
                            whiteSpace("pre")
                            fontFamily("Fira Code", "monospace")
                            fontSize(1.1.em)
                        }
                        value(value)
                        onInput { value = it.value }
                        onKeyDown { event ->
                            if (event.key == "Enter") {
                                event.preventDefault()
                                event.stopPropagation()
                                onEditConfirm(value)
                            }
                        }
                        onFocusOut { onEditConfirm(value) }
                    }
                )
            }
        }
        Button(
            attrs = {
                onClick { onNewFolderClick() }
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
    images: List<String>,
    imageIndex: Int,
    changeImageIndex: (Int) -> Unit
) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                marginTop(6.px)
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
                onClick { if (imageIndex != 0) changeImageIndex(imageIndex - 1) else changeImageIndex(images.size - 1) }
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
                Img(src = images[imageIndex]) {
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
                onClick { if (imageIndex != images.size - 1) changeImageIndex(imageIndex + 1) else changeImageIndex(0) }
            }
        ) {
            Text("›")
        }
    }
}

@Composable
fun BottomButtons(
    remove: () -> Unit,
    confirm: () -> Unit
) {
    val okButtonText = remember { getString(Values.OK) }
    var onTrashEnter by remember { mutableStateOf(false) }
    var onOkEnter by remember { mutableStateOf(false) }

    Div(
        attrs = {
            style {
                property("display", "flex")
                justifyContent(JustifyContent.SpaceBetween)
                paddingBottom(3.px)
                width(100.percent)
                height(40.px)
                backgroundColor(Color.transparent)
            }
        }
    ) {
        Button(
            attrs = {
                onClick { remove() }
                style {
                    marginTop(3.px)
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
                onClick { confirm() }
                style {
                    marginTop(3.px)
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
fun PopupTitle() {
    Div {
        H2(
            attrs = {
                style {
                    marginTop(8.px)
                    marginBottom(8.px)
                    paddingLeft(12.px)
                }
            }
        ) {
            Text(getString(Values.ADD_BOOKMARK))
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
            Text(getString(Values.OPEN_OPTIONS))
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