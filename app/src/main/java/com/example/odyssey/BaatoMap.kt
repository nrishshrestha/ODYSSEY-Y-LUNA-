package com.example.odyssey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.IconFactory
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.odyssey.utils.drawRoute

@Composable
fun BaatoMap(
    routePoints: List<LatLng>,
    currentLocation: LatLng?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context)
    }
    var userMarker: org.maplibre.android.annotations.Marker? = null

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                onCreate(null)
                getMapAsync { map ->
                    // 1. Determine the center point for the camera
                    val defaultLocation = LatLng(27.7172, 85.3240)
                    val targetLocation = currentLocation ?: defaultLocation

                    // 2. Set the initial camera position immediately
                    map.cameraPosition = CameraPosition.Builder()
                        .target(targetLocation)
                        .zoom(15.0)
                        .build()

                    // 3. Load Style - REPLACE WITH YOUR ACTUAL BAATO API KEY
                    // Get your key from https://baato.io/
                    val baatoApiKey = "bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"
                    val baatoStyleUrl = "https://api.baato.io/api/v1/styles/breeze?key=$baatoApiKey"

                    map.setStyle(Style.Builder().fromUri(baatoStyleUrl)) { style ->
                        android.util.Log.d("BaatoMap", "Map style loaded successfully")

                        // Add current location marker if available
                        currentLocation?.let { location ->
                            try {
                                val iconFactory = IconFactory.getInstance(context)
                                val icon = iconFactory.defaultMarker()

                                userMarker = map.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title("You are here")
                                )
                                userMarker?.icon = icon
                                android.util.Log.d("BaatoMap", "Current location marker added")
                            } catch (e: Exception) {
                                android.util.Log.e("BaatoMap", "Error adding marker: ${e.message}")
                            }
                        }

                        // Draw route if exists
                        if (routePoints.isNotEmpty()) {
                            drawRoute(map, routePoints)
                            android.util.Log.d("BaatoMap", "Route drawn with ${routePoints.size} points")
                        }
                    }
                }
            }
        },
        update = { view ->
            view.getMapAsync { map ->
                if (map.style?.isFullyLoaded == true) {
                    android.util.Log.d("BaatoMap", "Map update triggered")

                    // Update current location marker
                    currentLocation?.let { location ->
                        if (userMarker != null) {
                            userMarker?.position = location
                        } else {
                            try {
                                val iconFactory = IconFactory.getInstance(context)
                                val icon = iconFactory.defaultMarker()

                                userMarker = map.addMarker(
                                    MarkerOptions()
                                        .position(location)
                                        .title("You are here")
                                )
                                userMarker?.icon = icon
                            } catch (e: Exception) {
                                android.util.Log.e("BaatoMap", "Error updating marker: ${e.message}")
                            }
                        }

                        // Move camera to current location smoothly
                        map.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                location,
                                15.0
                            ),
                            1000 // 1 second animation
                        )
                    }

                    // Update route
                    if (routePoints.isNotEmpty()) {
                        drawRoute(map, routePoints)
                    }
                }
            }
        }
    )

    // Handle MapView lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
}