package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.AboutLibraries
import org.napharcos.bookmarkmanager.LibrariesData
import org.napharcos.bookmarkmanager.LicenseData

@Composable
fun LibrariesDialogContent() {
    var libraries by remember { mutableStateOf<List<LibrariesData>>(emptyList()) }

    LaunchedEffect(Unit) {
        val text = window.fetch("./aboutlibraries.json").await().text().await()

        val json = Json { ignoreUnknownKeys = true }
        libraries = json.decodeFromString<AboutLibraries>(text).toLibrariesData()
    }

    Div(attrs = {
        style {
            height(480.px)
            overflowY("auto")
        }
    }) {
        libraries.forEach {
            LibrariesElement(
                name = it.name,
                dev = it.dev,
                version = it.version,
                website = it.website,
                license = it.license
            )
        }
    }
}

@Composable
fun LibrariesElement(
    name: String,
    dev: String,
    version: String,
    website: String,
    license: LicenseData
) {
    var onEnter by remember { mutableStateOf(false) }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignContent(AlignContent.SpaceBetween)
            height(52.px)
            width(425.px)
            padding(4.px)
            backgroundColor(if (!onEnter) rgb(60, 60,60) else rgb(80, 80, 80))
        }
        onMouseEnter { onEnter = true }
        onMouseLeave { onEnter = false }
    }) {
        Div(attrs = {
            style {
                height(100.percent)
                flexGrow(2)
                alignContent(AlignContent.SpaceBetween)
                alignItems(AlignItems.Start)
            }
        }) {
            Div(attrs = {
                style {
                    textAlign("left")
                }
            }) {
                A(
                    attrs = {
                        style {
                            fontSize(1.2.em)
                            color(Color.white)
                            property("text-overflow", "ellipsis")
                            textAlign("left")
                        }
                        target(ATarget.Blank)
                    },
                    href = website
                ) {
                    Text(name)
                }
            }
            Div(attrs = {
                style {
                    fontSize(1.1.em)
                    property("text-overflow", "ellipsis")
                    textAlign("left")
                    paddingTop(4.px)
                }
            }) {
                Text(dev)
            }
        }
        Div(attrs = {
            style {
                height(100.percent)
                flexGrow(1)
                alignItems(AlignItems.End)
                justifyContent(JustifyContent.End)
            }
        }) {
            Div(attrs = {
                style {
                    property("text-overflow", "ellipsis")
                    textAlign("right")
                }
            }) {
                Text(version)
            }
            Div(attrs = {
                style {
                    textAlign("right")
                    paddingTop(4.px)
                }
            }) {
                A(
                    attrs = {
                        style {
                            height(25.px)
                            borderRadius(12.px)
                            backgroundColor(Color.blue)
                            paddingLeft(3.px)
                            paddingBottom(3.px)
                            color(Color.white)
                            property("text-overflow", "ellipsis")
                            textAlign("right")
                        }
                        target(ATarget.Blank)
                    },
                    href = license.url
                ) {
                    Text(license.hash)
                }
            }
        }
    }
}