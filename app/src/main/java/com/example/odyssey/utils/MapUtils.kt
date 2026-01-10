package com.example.odyssey.utils

import android.graphics.Color
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng

fun drawRoute(
    map: MapLibreMap,
    points: List<LatLng>
) {
    if (points.size < 2) return

    // Remove previous polylines only (don't clear the whole map — that removes markers)
    try {
        val existingPolylines = ArrayList(map.polylines)
        for (poly in existingPolylines) {
            map.removePolyline(poly)
        }
    } catch (e: Exception) {
        // If anything goes wrong, fall back to a safe approach — don't crash the app.
        android.util.Log.w("MapUtils", "Couldn't remove existing polylines: ${e.message}")
    }

    // Draw the new polyline
    map.addPolyline(
        PolylineOptions()
            .addAll(points)
            .color(Color.RED)
            .width(5f)
    )
}