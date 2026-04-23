package com.afquintana.buycheaper

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BuyCheaperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
}
