package org.napharcos.bookmarkmanager.popup

data class PageData(
    val title: String,
    val url: String,
    val images: List<String>
)

fun PageInfo.toPageData(screenShot: String = "", generatedImage: String = ""): PageData {
    val allowedExtensions = listOf(".png", ".jpg", ".jpeg", ".svg", "data:image/jpg", "data:image/png", "data:image/jpeg", "data:image/svg+xml")
    val images = mutableListOf(screenShot, generatedImage)
    images.addAll(this.pageImages)
    images.addAll(this.metaImages)
    images.addAll(this.favicons)

    return PageData(
        title = this.title,
        url = this.fullUrl,
        images = images.filter { it.isNotEmpty() && allowedExtensions.any { e -> it.lowercase().endsWith(e) || it.startsWith(e) } }
    )
}