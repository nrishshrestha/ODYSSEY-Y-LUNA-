package com.example.odyssey

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox

class OdysseyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Mapbox.getInstance(
            this,
            "bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"
        )
    }
}