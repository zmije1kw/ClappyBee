package com.example.clappybee

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.clappybee.di.initializeKoin
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeKoin()
    ComposeViewport(document.body!!) {
        App()
    }
}