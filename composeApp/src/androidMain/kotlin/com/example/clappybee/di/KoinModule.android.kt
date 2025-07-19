package com.example.clappybee.di

import com.example.clappybee.domain.AudioPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> { AudioPlayer(context = androidContext()) }
}