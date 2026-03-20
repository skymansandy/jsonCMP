package dev.skymansandy.jsoncmpsample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.jsoncmpsample.ui.App

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "JsonCMP Sample",
        ) {
            App()
        }
    }
}
