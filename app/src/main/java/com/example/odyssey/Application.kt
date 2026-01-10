package com.example.odyssey

import android.app.Application
import org.maplibre.android.MapLibre

class OdysseyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}