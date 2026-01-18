package com.example.odyssey.model

import org.maplibre.android.geometry.LatLng

data class RouteModel(
    val routeId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val points: List<RoutePointModel> = emptyList(),
    val isLive: Boolean = false,
    val totalDistance: Double = 0.0,
    val duration: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "routeId" to routeId,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "createdAt" to createdAt,
            "points" to points.map { it.toMap() },
            "isLive" to isLive,
            "totalDistance" to totalDistance,
            "duration" to duration
        )
    }
}

data class RoutePointModel(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val photoUrl: String = "", // Cloudinary URL
    val note: String = "",
    val speed: Float = 0f,
    val bearing: Float = 0f
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)

    fun hasAttachments(): Boolean = photoUrl.isNotEmpty() || note.isNotEmpty()

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to timestamp,
            "photoUrl" to photoUrl,
            "note" to note,
            "speed" to speed,
            "bearing" to bearing
        )
    }
}