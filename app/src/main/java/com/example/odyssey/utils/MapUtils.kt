package com.example.odyssey.utils

import android.graphics.Color
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

fun drawRoute(
    mapboxMap: MapboxMap,
    points: List<LatLng>
) {
    if (points.size < 2) return

    val polylineOptions = PolylineOptions()
        .addAll(points)
        .color(Color.RED)
        .width(5f)

    mapboxMap.clear()
    mapboxMap.addPolyline(polylineOptions)
}
