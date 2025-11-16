package org.napharcos.bookmarkmanager.database

import com.juul.indexeddb.Database
import com.juul.indexeddb.Key
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.deleteDatabase
import com.juul.indexeddb.logs.Logger
import com.juul.indexeddb.logs.Type
import com.juul.indexeddb.openDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.napharcos.bookmarkmanager.AppScope
import org.napharcos.bookmarkmanager.Bookmarks
import org.napharcos.bookmarkmanager.data.Constants
import org.w3c.dom.events.Event
import kotlin.js.unsafeCast

@OptIn(DelicateCoroutinesApi::class)
class BrowserDBManager(): DatabaseRepository {

    companion object {
        const val DB_NAME = "bookmarks_data"
        const val TABLE_NAME = "bookmarks"
    }

    private var deferredDatabase: Deferred<Database>? = null

    private fun getDatabase(scope: CoroutineScope): Deferred<Database> {
        if (deferredDatabase == null) {
            deferredDatabase = scope.async(start = CoroutineStart.LAZY) {
                openDatabase(DB_NAME, 1) { db, old, /*new*/_ ->
                    if (old < 1) {
                        val store = db.createObjectStore(TABLE_NAME, KeyPath("uuid"))
                        store.createIndex("parentId", KeyPath("parentId"), false)
                        store.createIndex("name", KeyPath("name"), false)
                        store.createIndex("created", KeyPath("created"), false)
                        store.createIndex("modified", KeyPath("modified"), false)
                        store.createIndex("type", KeyPath("type"), false)
                        store.createIndex("url", KeyPath("url"), false)
                        store.createIndex("index", KeyPath("index"), false)
                        store.createIndex("imageId", KeyPath("imageId"), false)
                        store.createIndex("parentId_type", KeyPath("parentId", "type"), false)
                    }
                }
            }
        }
        return deferredDatabase!!
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun deleteDB() {
        deferredDatabase?.takeIf { it.isCompleted }?.getCompleted()?.close()
        deferredDatabase = null
        deleteDatabase(DB_NAME, ConsoleLogger)
    }

    override fun addBookmark(bookmark: Bookmarks, override: Boolean) {
        AppScope.scope.launch {
            val database = getDatabase(this).await()

            database.writeTransaction(TABLE_NAME) {
                val store = objectStore(TABLE_NAME)

                if (override)
                    store.put(bookmark)
                else store.add(bookmark)
            }
        }
    }

    override suspend fun updateImage(coroutine: CoroutineScope, uuid: String, image: String) {
        val db = getDatabase(coroutine).await()

        db.writeTransaction(TABLE_NAME) {
            val store = objectStore(TABLE_NAME)
            val existing = store.get(Key(uuid))
                ?.unsafeCast<Bookmarks>()

            if (existing != null) {
                existing.asDynamic().image = image
                store.put(existing)
            } else {
                console.warn("No record found: $uuid")
            }
        }
    }

    override suspend fun getSpecificFolders(scope: CoroutineScope, parentId: String): List<Bookmarks> {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            val results = mutableListOf<Bookmarks>()

            val cursor = objectStore(TABLE_NAME)
                .index("parentId_type")
                .openCursor(query = Key(parentId, Constants.FOLDER), autoContinue = true)

            cursor.collect {
                results.add(it.value.unsafeCast<Bookmarks>())
            }

            results
        }
    }

    override suspend fun getFolders(scope: CoroutineScope): List<Bookmarks> {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            val results = mutableListOf<Bookmarks>()

            val cursor = objectStore(TABLE_NAME)
                .index("type")
                .openCursor(query = Key(Constants.FOLDER), autoContinue = true)

            cursor.collect {
                results.add(it.value.unsafeCast<Bookmarks>())
            }

            results
        }
    }

    override suspend fun getChilds(scope: CoroutineScope, parentId: String): List<Bookmarks> {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            val results = mutableListOf<Bookmarks>()

            val cursor = objectStore(TABLE_NAME)
                .index("parentId")
                .openCursor(query = Key(parentId), autoContinue = true)

            cursor.collect {
                results.add(it.value.unsafeCast<Bookmarks>())
            }

            results
        }
    }

    override suspend fun getBookmark(scope: CoroutineScope, uuid: String): Bookmarks? {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            objectStore(TABLE_NAME)
                .get(Key(uuid))
                ?.unsafeCast<Bookmarks>()
        }
    }

    override suspend fun deleteBookmark(scope: CoroutineScope, uuid: String) {
        val database = getDatabase(scope).await()

        database.writeTransaction(TABLE_NAME) {
            objectStore(TABLE_NAME).delete(Key(uuid))
        }
    }


    override suspend fun getBookmarkByImage(scope: CoroutineScope, imageId: String): Bookmarks? {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            objectStore(TABLE_NAME)
                .index("imageId")
                .get(Key(imageId))
                ?.unsafeCast<Bookmarks>()
        }
    }

    override suspend fun getBookmarkByUrl(scope: CoroutineScope, url: String): Bookmarks? {
        val database = getDatabase(scope).await()

        return database.transaction(TABLE_NAME) {
            objectStore(TABLE_NAME)
                .index("url")
                .get(Key(url))
                ?.unsafeCast<Bookmarks>()
        }
    }

    object ConsoleLogger: Logger {
        override fun log(type: Type, event: Event?, message: () -> String) { console.log(message) }
    }
}