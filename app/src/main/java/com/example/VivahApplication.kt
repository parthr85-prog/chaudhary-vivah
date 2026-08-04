package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class VivahApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val apiKey = BuildConfig.FIREBASE_API_KEY.ifEmpty { "AIzaSyCCpmeFoPbFJbcmyFp1Tzvqm5z63XK5jxU" }
                val appId = BuildConfig.FIREBASE_APP_ID.ifEmpty { "1:685467333286:web:151bf91598efe33114ba67" }
                val projectId = BuildConfig.FIREBASE_PROJECT_ID.ifEmpty { "application-vivah" }
                val storageBucket = BuildConfig.FIREBASE_STORAGE_BUCKET.ifEmpty { "application-vivah.firebasestorage.app" }
                val senderId = BuildConfig.FIREBASE_MESSAGING_SENDER_ID.ifEmpty { "685467333286" }

                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .setGcmSenderId(senderId)
                    .build()

                FirebaseApp.initializeApp(this, options)
                Log.d("VivahApplication", "Firebase successfully initialized for project: $projectId")
            }
        } catch (e: Exception) {
            Log.e("VivahApplication", "Firebase initialization error", e)
        }
    }
}
