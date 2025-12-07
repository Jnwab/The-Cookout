package com.cecs491b.thecookout

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TheCookoutApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("TheCookoutApp", "🔥 App starting...")

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Log.d("TheCookoutApp", "🔥 Setting up emulators")
            Firebase.firestore.useEmulator("10.0.7.92", 8080)
            Firebase.auth.useEmulator("10.0.7.92", 9100)
            Firebase.storage.useEmulator("10.0.7.92", 9199)
            Log.d("TheCookoutApp", "✅ Emulators ready!")
        }
    }
}