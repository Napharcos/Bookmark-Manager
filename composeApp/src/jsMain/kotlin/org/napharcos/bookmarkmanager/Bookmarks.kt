@file:Suppress("FunctionName")

package org.napharcos.bookmarkmanager

import kotlin.js.Date
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class BookmarksTree(
    val folder: Bookmarks,
    val children: List<BookmarksTree>
)

fun List<Bookmarks>.buildTree(): List<BookmarksTree> =
    this.sortedBy { it.index }.groupBy { it.parentId }.buildTree()

private fun Map<String, List<Bookmarks>>.buildTree(parentId: String = ""): List<BookmarksTree> {
    return this[parentId]?.map {
        BookmarksTree(
            folder = it,
            children = this.buildTree(it.uuid)
        )
    } ?: emptyList()
}

external interface Bookmarks {
    var uuid: String
    var parentId: String
    var name: String
    var modified: String
    var type: String
    var url: String
    var index: Int
    var imageId: String
    var image: String
    var undoTrash: String
    //var changed: Boolean
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Bookmark(
    uuid: String = Uuid.random().toHexString(),
    parentId: String = "",
    name: String,
    modified: String = Clock.System.now().toEpochMilliseconds().toString(),
    type: String,
    url: String = "",
    index: Int,
    imageId: String,
    image: String,
    undoTrash: String = "",
    //changed: Boolean = true
): Bookmarks {
    val bookmark = js("{}").unsafeCast<Bookmarks>()
    bookmark.uuid = uuid
    bookmark.parentId = parentId
    bookmark.name = name
    bookmark.modified = modified
    bookmark.url = url
    bookmark.type = type
    bookmark.index = index
    bookmark.imageId = imageId
    bookmark.image = image
    bookmark.undoTrash = undoTrash
    //bookmark.changed = changed
    return bookmark
}

fun Bookmarks.copy(
    uuid: String = this.uuid,
    parentId: String = this.parentId,
    name: String = this.name,
    modified: String = this.modified,
    type: String = this.type,
    url: String = this.url,
    index: Int = this.index,
    imageId: String = this.imageId,
    image: String = this.image,
    undoTrash: String = this.undoTrash,
    //changed: Boolean = this.changed
): Bookmarks {
    return Bookmark(
        uuid = uuid,
        parentId = parentId,
        name = name,
        modified = modified,
        type = type,
        url = url,
        index = index,
        imageId = imageId,
        image = image,
        undoTrash = undoTrash,
        //changed = changed
    )
}

@OptIn(ExperimentalTime::class)
fun Long.convertChromeTime(): Long {
    return Date(Date.UTC(1601, 0, 1) + this / 1000).toKotlinInstant().toEpochMilliseconds()
}

fun Long.toChromeTime(): Long {
    val epochDiffMillis = 11_644_473_600_000L // ms between 1601-01-01 and 1970-01-01
    return (this + epochDiffMillis) * 1_000L
}