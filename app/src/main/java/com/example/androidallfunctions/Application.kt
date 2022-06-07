package com.example.androidallfunctions

import android.app.Application
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        // EmojiEdittext
        EmojiManager.install(IosEmojiProvider())
    }
}