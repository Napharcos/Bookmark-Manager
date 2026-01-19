const DB_NAME = "bookmarks_data"
const TABLE_NAME = "bookmarks"

async function updateIconByUrl() {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true })
    if (!tab || !tab.url) return
    const currentUrl = tab.url

    const dbRequest = indexedDB.open(DB_NAME, 2)

    dbRequest.onupgradeneeded = event => {
        dbRequest.transaction.abort()
    }

    dbRequest.onsuccess = event => {
        const db = event.target.result

        if (!db.objectStoreNames.contains(TABLE_NAME)) {
            db.close()
            return
        }

        const tx = db.transaction(TABLE_NAME, "readonly")
        const store = tx.objectStore(TABLE_NAME)

        if (!store.indexNames.contains("url")) {
            db.close()
            return
        }

        const index = store.index("url")
        const getReq = index.get(currentUrl)

        getReq.onsuccess = () => {
            const found = !!getReq.result
            chrome.action.setIcon({
                path: found ? {
                    16: "icons/bookmark_16.png",
                    32: "icons/bookmark_32.png",
                    48: "icons/bookmark_48.png",
                    128: "icons/bookmark_128.png"
                } : {
                    16: "icons/bookmark_border_16.png",
                    32: "icons/bookmark_border_32.png",
                    48: "icons/bookmark_border_48.png",
                    128: "icons/bookmark_border_128.png"
                },
                tabId: tab.id
            })
        }
    }
}

chrome.tabs.onActivated.addListener(updateIconByUrl)
chrome.tabs.onUpdated.addListener((_, info, tab) => {
    if (info.status === "complete" && tab.active) updateIconByUrl()
})