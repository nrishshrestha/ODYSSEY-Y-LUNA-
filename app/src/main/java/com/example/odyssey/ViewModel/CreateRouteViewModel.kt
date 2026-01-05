package com.example.odyssey.ViewModel

import android.Manifest
import android.app.Application
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.geometry.LatLng

class CreateRouteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val locationClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        3000L
    ).build()

    private val _routePoints = mutableStateListOf<LatLng>()
    val routePoints: List<LatLng> = _routePoints

    var isRecording by mutableStateOf(false)
        private set

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                if (isRecording) {
                    _routePoints.add(
                        LatLng(it.latitude, it.longitude)
                    )
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startRecording() {
        isRecording = true
        _routePoints.clear()

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopRecording() {
        isRecording = false
        locationClient.removeLocationUpdates(locationCallback)
    }
}
