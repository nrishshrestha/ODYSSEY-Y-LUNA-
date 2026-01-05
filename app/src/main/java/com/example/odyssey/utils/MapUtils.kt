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

    // Clear previous annotations
    map.clear()

    // Draw polyline
    map.addPolyline(
        PolylineOptions()
            .addAll(points)
            .color(Color.RED)
            .width(5f)
    )
}