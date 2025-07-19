package com.example.clappybee.domain

data class PipePair(
    var x: Float,
    var y: Float,
    val topHeight: Float,
    val bottomHeight: Float,
    var scored: Boolean = false
)
