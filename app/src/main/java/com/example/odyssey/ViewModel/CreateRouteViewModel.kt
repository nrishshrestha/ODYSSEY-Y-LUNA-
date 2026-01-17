package com.example.odyssey.ViewModel

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase  // ← Changed
import org.maplibre.android.geometry.LatLng
import com.example.odyssey.model.RoutePointModel
import com.example.odyssey.model.RouteModel
import com.example.odyssey.utils.CloudinaryManager
import java.util.UUID

class CreateRouteViewModel : ViewModel() {

    // Observable state
    var routePoints = mutableStateListOf<RoutePointModel>()
        private set

    var currentLocation = mutableStateOf<LatLng?>(null)
        private set

    var isRecording = mutableStateOf(false)
        private set

    // Upload state
    var isUploadingPhoto = mutableStateOf(false)
        private set

    var uploadProgress = mutableStateOf(0)
        private set

    var uploadError = mutableStateOf<String?>(null)
        private set

    // Dialog state
    var showNoteDialog = mutableStateOf(false)
        private set

    var showSaveRouteDialog = mutableStateOf(false)
        private set

    var pendingNote = mutableStateOf("")
        private set

    var routeTitle = mutableStateOf("")
        private set

    var routeDescription = mutableStateOf("")
        private set

    // Recording metadata
    private var recordingStartTime = 0L

    // Firebase - Using Realtime Database instead of Firestore
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Location tracking
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    // === LOCATION TRACKING ===

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
                    startLocationUpdatesForCurrent(context)
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("CreateRouteViewModel", "Location permission error", e)
        }
    }

    private fun startLocationUpdatesForCurrent(context: Context) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMaxUpdates(1).build()

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

    // === RECORDING CONTROL ===

    fun startRecording(context: Context) {
        android.util.Log.d("CreateRouteViewModel", "startRecording called")

        if (isRecording.value) {
            android.util.Log.d("CreateRouteViewModel", "Already recording, returning")
            return
        }

        isRecording.value = true
        routePoints.clear()
        recordingStartTime = System.currentTimeMillis()

        android.util.Log.d("CreateRouteViewModel", "Recording started at $recordingStartTime")

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    currentLocation.value = latLng

                    val point = RoutePointModel(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speed = location.speed,
                        bearing = location.bearing
                    )
                    routePoints.add(point)

                    android.util.Log.d("CreateRouteViewModel",
                        "New point: ${location.latitude}, ${location.longitude}. Total: ${routePoints.size}")
                }
            }
        }

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

    fun stopRecording() {
        isRecording.value = false
        locationCallback?.let {
            fusedLocationClient?.removeLocationUpdates(it)
        }
        locationCallback = null

        val duration = System.currentTimeMillis() - recordingStartTime
        android.util.Log.d("CreateRouteViewModel",
            "Recording stopped. Total points: ${routePoints.size}, Duration: ${duration}ms")

        if (routePoints.isNotEmpty()) {
            showSaveRouteDialog.value = true
        }
    }

    // === NOTE FUNCTIONALITY ===

    fun openNoteDialog() {
        pendingNote.value = ""
        showNoteDialog.value = true
    }

    fun closeNoteDialog() {
        showNoteDialog.value = false
        pendingNote.value = ""
    }

    fun updatePendingNote(text: String) {
        pendingNote.value = text
    }

    fun saveNote() {
        if (pendingNote.value.isNotBlank() && currentLocation.value != null) {
            val notePoint = RoutePointModel(
                latitude = currentLocation.value!!.latitude,
                longitude = currentLocation.value!!.longitude,
                note = pendingNote.value
            )
            routePoints.add(notePoint)

            android.util.Log.d("CreateRouteViewModel",
                "Note added: '${pendingNote.value}' at ${currentLocation.value}. Total: ${routePoints.size}")
        }
        closeNoteDialog()
    }

    // === PHOTO FUNCTIONALITY ===

    fun uploadAndSavePhoto(context: Context, photoUri: Uri) {
        if (currentLocation.value == null) {
            uploadError.value = "Location not available"
            return
        }

        isUploadingPhoto.value = true
        uploadProgress.value = 0
        uploadError.value = null

        val savedLocation = currentLocation.value!!

        android.util.Log.d("CreateRouteViewModel", "Starting photo upload for location: $savedLocation")

        CloudinaryManager.uploadRoutePhoto(
            context = context,
            imageUri = photoUri,
            onProgress = { progress ->
                uploadProgress.value = progress
                android.util.Log.d("CreateRouteViewModel", "Upload progress: $progress%")
            },
            onSuccess = { cloudinaryUrl ->
                android.util.Log.d("CreateRouteViewModel", "Photo uploaded successfully: $cloudinaryUrl")

                val photoPoint = RoutePointModel(
                    latitude = savedLocation.latitude,
                    longitude = savedLocation.longitude,
                    photoUrl = cloudinaryUrl
                )
                routePoints.add(photoPoint)

                android.util.Log.d("CreateRouteViewModel",
                    "Photo point added at $savedLocation. Total: ${routePoints.size}")

                isUploadingPhoto.value = false
                uploadProgress.value = 0
            },
            onError = { exception ->
                android.util.Log.e("CreateRouteViewModel", "Photo upload failed", exception)
                uploadError.value = exception.message
                isUploadingPhoto.value = false
                uploadProgress.value = 0
            }
        )
    }

    // === ROUTE SAVING (USING REALTIME DATABASE) ===

    fun openSaveRouteDialog() {
        routeTitle.value = ""
        routeDescription.value = ""
        showSaveRouteDialog.value = true
    }

    fun closeSaveRouteDialog() {
        showSaveRouteDialog.value = false
        routeTitle.value = ""
        routeDescription.value = ""
    }

    fun updateRouteTitle(text: String) {
        routeTitle.value = text
    }

    fun updateRouteDescription(text: String) {
        routeDescription.value = text
    }

    fun saveRouteToFirebase(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError(Exception("User not logged in"))
            return
        }

        if (routeTitle.value.isBlank()) {
            onError(Exception("Route title is required"))
            return
        }

        if (routePoints.isEmpty()) {
            onError(Exception("No route points to save"))
            return
        }

        val routeId = UUID.randomUUID().toString()
        val duration = System.currentTimeMillis() - recordingStartTime

        val route = RouteModel(
            routeId = routeId,
            userId = userId,
            title = routeTitle.value,
            description = routeDescription.value,
            createdAt = recordingStartTime,
            points = routePoints.toList(),
            isLive = false,
            duration = duration,
            totalDistance = calculateTotalDistance()
        )

        android.util.Log.d("CreateRouteViewModel", "Saving route to Realtime Database: ${route.title}")

        // Save to Realtime Database
        database.reference
            .child("routes")
            .child(routeId)
            .setValue(route.toMap())
            .addOnSuccessListener {
                android.util.Log.d("CreateRouteViewModel", "Route saved successfully: $routeId")
                closeSaveRouteDialog()
                routePoints.clear()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("CreateRouteViewModel", "Failed to save route", exception)
                onError(exception)
            }
    }

    private fun calculateTotalDistance(): Double {
        if (routePoints.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until routePoints.size - 1) {
            val from = routePoints[i].toLatLng()
            val to = routePoints[i + 1].toLatLng()
            totalDistance += from.distanceTo(to)
        }
        return totalDistance / 1000.0
    }

    fun getLatLngPoints(): List<LatLng> {
        return routePoints.map { it.toLatLng() }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        fusedLocationClient = null
    }
}