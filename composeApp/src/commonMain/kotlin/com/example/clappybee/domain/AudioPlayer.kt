package com.example.clappybee.domain

expect class AudioPlayer {
    fun playGameOverSound()
    fun playJumpSound()
    fun playFallingSound()
    fun stopFallingSound()
    fun playGameSoundInLoop()
    fun stopGameSound()
    fun release()
}

val soundResList = listOf(
    "files/falling.wav",
    "files/game_over.wav",
    "files/game_sound.wav",
    "files/jump.wav"
)