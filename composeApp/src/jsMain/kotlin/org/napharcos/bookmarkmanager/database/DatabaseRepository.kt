package org.napharcos.bookmarkmanager.database

import kotlinx.coroutines.CoroutineScope
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.FileSystemDirectoryHandle

interface DatabaseRepository {

    suspend fun deleteDB()
    fun addBookmark(bookmark: Bookmarks, override: Boolean = false)

    suspend fun updateImage(coroutine: CoroutineScope, uuid: String, image: String)

    suspend fun getFolders(scope: CoroutineScope): List<Bookmarks>

    suspend fun getSpecificFolders(scope: CoroutineScope, parentId: String): List<Bookmarks>

    suspend fun getChilds(scope: CoroutineScope, parentId: String): List<Bookmarks>

    suspend fun getBookmark(scope: CoroutineScope, uuid: String): Bookmarks?

    suspend fun deleteBookmark(scope: CoroutineScope, uuid: String)

    suspend fun getBookmarkByImage(scope: CoroutineScope, imageId: String): Bookmarks?

    suspend fun getBookmarkByUrl(scope: CoroutineScope, url: String): Bookmarks?

    suspend fun getBackupDir(scope: CoroutineScope): FileSystemDirectoryHandle?

    fun saveBackupDir(dir: FileSystemDirectoryHandle)

    suspend fun getBackupFiles(scope: CoroutineScope): String

    fun saveBackupFiles(fileNames: String)
}