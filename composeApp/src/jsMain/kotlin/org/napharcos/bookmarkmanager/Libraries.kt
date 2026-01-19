package org.napharcos.bookmarkmanager

import kotlinx.serialization.Serializable

@Serializable
data class AboutLibraries(
    val libraries: List<Library>,
    val licenses: Map<String, License>
) {
    fun toLibrariesData() : List<LibrariesData> {
        return libraries.map {
            val license = licenses[it.licenses[0]]
            LibrariesData(
                name = it.name,
                dev = it.developers[0].name,
                version = it.artifactVersion,
                license = LicenseData(
                    hash = license?.hash ?: "",
                    url = license?.url ?: ""
                ),
                website = it.website
            )
        }
    }
}

@Serializable
data class Library(
    val uniqueId: String,
    val developers: List<Developer>,
    val artifactVersion: String,
    val name: String,
    val website: String,
    val licenses: List<String>
)

@Serializable
data class Developer(val name: String)

@Serializable
data class License(
    val hash: String,
    val url: String
)

data class LicenseData(
    val hash: String,
    val url: String,
)

data class LibrariesData(
    val name: String,
    val dev: String,
    val version: String,
    val license: LicenseData,
    val website: String
)