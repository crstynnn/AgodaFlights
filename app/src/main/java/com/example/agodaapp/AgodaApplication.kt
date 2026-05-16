package com.example.agodaapp

import android.app.Application
import com.google.firebase.FirebaseApp

class AgodaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}