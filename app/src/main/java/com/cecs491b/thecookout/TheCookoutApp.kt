package com.cecs491b.thecookout

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage

class TheCookoutApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("TheCookoutApp", "🔥 App starting...")

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Log.d("TheCookoutApp", "🔥 Setting up emulators")
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.auth.useEmulator("10.0.2.2", 9100)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
            Log.d("TheCookoutApp", "✅ Emulators ready!")
        }
    }
}