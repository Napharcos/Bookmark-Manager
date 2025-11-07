package org.napharcos.bookmarkmanager.popup

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.napharcos.bookmarkmanager.AppScope
import org.napharcos.bookmarkmanager.Bookmark
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.data.Constants

class PopupViewModel {

    val folders = mutableStateMapOf<String, Bookmarks>()

    val openFolders = mutableStateListOf<String>()

    init {
        AppScope.scope.launch {
            flowOf<Bookmarks>(
                BOOKMARK_TEST_1,
                BOOKMARK_TEST_2,
                BOOKMARK_TEST_3,
                BOOKMARK_TEST_1_2,
                BOOKMARK_TEST_4,
                BOOKMARK_TEST_2_1,
                BOOKMARK_TEST_2_2,
                BOOKMARK_TEST_10_1,
                BOOKMARK_TEST_10_2,
                BOOKMARK_TEST_10_3,
                BOOKMARK_TEST_10_4,
                BOOKMARK_TEST_10_6,
                BOOKMARK_TEST_10_5
            ).collectLatest {
                folders[it.uuid] = it
            }
        }
    }

    fun openFolder(uuid: String) = openFolders.add(uuid)

    fun closeFolder(uuid: String) = openFolders.remove(uuid)

    private val BOOKMARK_TEST_1 = Bookmark(
        name = "Bookmark Folder 1",
        type = Constants.FOLDER,
        index = 0,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_1_2 = Bookmark(
        parentId = BOOKMARK_TEST_1.uuid,
        name = "Bookmark Folder 1_2",
        type = Constants.FOLDER,
        index = 0,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_2 = Bookmark(
        parentId = BOOKMARK_TEST_1.uuid,
        name = "Bookmark Folder 1_1",
        type = Constants.FOLDER,
        index = 1,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_3 = Bookmark(
        name = "Bookmark Folder 2",
        type = Constants.FOLDER,
        index = 1,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_4 = Bookmark(
        name = "Bookmark Folder 3",
        type = Constants.FOLDER,
        index = 2,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_2_1 = Bookmark(
        parentId = BOOKMARK_TEST_2.uuid,
        name = "Bookmark Folder 2_1",
        type = Constants.FOLDER,
        index = 0,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_2_2 = Bookmark(
        parentId = BOOKMARK_TEST_2.uuid,
        name = "Bookmark Folder 2_2",
        type = Constants.FOLDER,
        index = 1,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_1 = Bookmark(
        name = "Bookmark Folder 10_1",
        type = Constants.FOLDER,
        index = 3,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_2 = Bookmark(
        name = "Bookmark Folder 10_2",
        type = Constants.FOLDER,
        index = 4,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_3 = Bookmark(
        name = "Bookmark Folder 10_3",
        type = Constants.FOLDER,
        index = 5,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_4 = Bookmark(
        name = "Bookmark Folder 10_4",
        type = Constants.FOLDER,
        index = 6,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_5 = Bookmark(
        name = "Bookmark Folder 10_5",
        type = Constants.FOLDER,
        index = 7,
        image = "./folder.svg",
        imageId = "",
    )
    private val BOOKMARK_TEST_10_6 = Bookmark(
        name = "Bookmark Folder 10_6",
        type = Constants.FOLDER,
        index = 8,
        image = "./folder.svg",
        imageId = "",
    )
}