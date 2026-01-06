package com.example.odyssey.ViewModel

import android.content.Context
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import org.maplibre.android.geometry.LatLng

class CreateRouteViewModel : ViewModel() {

    // Observable state
    var routePoints = mutableStateListOf<LatLng>()
        private set

    var isRecording = mutableStateOf(false)
        private set

    // Location tracking
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    // Start recording
    fun startRecording(context: Context) {
        if (isRecording.value) return

        isRecording.value = true
        routePoints.clear()

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Location request settings
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Update every 5 seconds
        ).apply {
            setMinUpdateIntervalMillis(2000L) // Fastest: 2 seconds
            setWaitForAccurateLocation(true)
        }.build()

        // Location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    routePoints.add(latLng)

                    // Log for debugging
                    android.util.Log.d("CreateRouteViewModel",
                        "New point: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        // Start location updates
        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            android.util.Log.e("CreateRouteViewModel", "Location permission error", e)
            stopRecording()
        }
    }

    // Stop recording
    fun stopRecording() {
        isRecording.value = false
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
        fusedLocationClient = null
        locationCallback = null

        android.util.Log.d("CreateRouteViewModel",
            "Recording stopped. Total points: ${routePoints.size}")
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
}