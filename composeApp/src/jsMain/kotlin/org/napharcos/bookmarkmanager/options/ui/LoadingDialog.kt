package org.napharcos.bookmarkmanager.options.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.napharcos.bookmarkmanager.dialogBackground

@Composable
fun LoadingDialog(
    loadingText: String
) {
    Div(
        attrs = {
            style { dialogBackground() }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    justifyContent(JustifyContent.SpaceBetween)
                    backgroundColor(rgb(60, 60, 60))
                    borderRadius(12.px)
                    padding(4.px)
                    width(300.px)
                    height(220.px)
                    textAlign("center")
                    property("box-shadow", "rgba(0, 0, 0, 0.3) 0px 4px 12px")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            Div(
                attrs = {
                    style {
                        width(100.percent)
                        height(100.percent)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        justifyContent(JustifyContent.Center)
                        alignContent(AlignContent.Center)
                    }
                }
            ) {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignSelf(AlignSelf.Center)
                    }
                    classes("loader")
                })
                Div(
                    attrs = {
                        style {
                            paddingTop(20.px)
                            paddingBottom(8.px)
                            paddingLeft(8.px)
                            paddingRight(8.px)
                            fontSize(1.2.em)
                            whiteSpace("nowrap")
                            property("text-overflow", "ellipsis")
                            overflow("hidden")
                        }
                    }
                ) {
                    Text(loadingText)
                }
            }
        }
    }
}