package com.example.clappybee.util

enum class Platform {
    Android,
    iOS,
    Desktop,
    Web
}

expect fun getPlatform(): Platform