package com.example.clappybee

import androidx.compose.ui.window.ComposeUIViewController
import com.example.clappybee.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }