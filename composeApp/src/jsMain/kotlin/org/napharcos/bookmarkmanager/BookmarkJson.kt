package org.napharcos.bookmarkmanager

import kotlinx.serialization.Serializable

@Serializable
data class BookmarkJson(
    val checksum: String,
    val roots: BookmarkJsonRoots,
    val version: Int = 1
)

@Serializable
data class BookmarkJsonRoots(
    val bookmark_bar: BookmarkData,
    val custom_root: CustomRoot? = null,
    val other: BookmarkData,
    val synced: BookmarkData,
    val trash: BookmarkData? = null
)

//opera only
@Serializable
data class CustomRoot(
    val pinboard: BookmarkData? = null,
    val speedDial: BookmarkData? = null,
    val trash: BookmarkData? = null,
    val unsorted: BookmarkData? = null,
    val unsyncedPinboard: BookmarkData? = null,
    val userRoot: BookmarkData? = null
)

@Serializable
data class BookmarkData(
    val children: List<BookmarkData> = emptyList(),
    val date_added: String = "",
    val date_last_used: String = "0",
    val date_modified: String = "",
    val guid: String = "",
    val id: String = "",
    val meta_info: BookmarkMeta? = null,
    val name: String = "",
    val type: String = "",
    val url: String = ""
)

@Serializable
data class BookmarkMeta(
    val imageID: String = "",
    val Thumbnail: String = "",
    val undoTrashParentId: String = ""
)