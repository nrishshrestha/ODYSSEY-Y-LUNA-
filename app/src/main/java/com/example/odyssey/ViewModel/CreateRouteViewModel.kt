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

    var currentLocation = mutableStateOf<LatLng?>(null)
        private set

    var isRecording = mutableStateOf(false)
        private set

    // Location tracking
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    // Get current location once (for initial map position)
    fun getCurrentLocation(context: Context) {
        android.util.Log.d("CreateRouteViewModel", "Getting current location...")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation.value = LatLng(location.latitude, location.longitude)
                    android.util.Log.d("CreateRouteViewModel",
                        "Current location: ${location.latitude}, ${location.longitude}")
                } else {
                    android.util.Log.d("CreateRouteViewModel", "Last location is null, requesting updates")
                    // If no last location, request one update
                    startLocationUpdatesForCurrent(context)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("CreateRouteViewModel", "Location permission error", e)
        }
    }

    // Request location updates just to get current location
    private fun startLocationUpdatesForCurrent(context: Context) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMaxUpdates(1).build() // Only one update

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation.value = LatLng(location.latitude, location.longitude)
                    android.util.Log.d("CreateRouteViewModel",
                        "Got current location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            android.util.Log.e("CreateRouteViewModel", "Location permission error", e)
        }
    }

    // Start recording route
    fun startRecording(context: Context) {
        android.util.Log.d("CreateRouteViewModel", "startRecording called")

        if (isRecording.value) {
            android.util.Log.d("CreateRouteViewModel", "Already recording, returning")
            return
        }

        isRecording.value = true
        routePoints.clear()

        android.util.Log.d("CreateRouteViewModel", "Recording started, routePoints cleared")

        // Initialize location client if not already
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

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

                    // Update current location
                    currentLocation.value = latLng

                    // Add to route
                    routePoints.add(latLng)

                    android.util.Log.d("CreateRouteViewModel",
                        "New point: ${location.latitude}, ${location.longitude}. Total: ${routePoints.size}")
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
            android.util.Log.d("CreateRouteViewModel", "Location updates started")
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
        locationCallback = null

        android.util.Log.d("CreateRouteViewModel",
            "Recording stopped. Total points: ${routePoints.size}")
    }

    // Clean up
    override fun onCleared() {
        super.onCleared()
        stopRecording()
        fusedLocationClient = null
    }
}