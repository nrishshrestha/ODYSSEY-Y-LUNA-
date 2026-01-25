package com.example.odyssey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.IconFactory
import android.graphics.Color
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

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                onCreate(null)
                getMapAsync { map ->
                    // Default camera position (Kathmandu, Nepal)
                    val defaultLocation = LatLng(27.7172, 85.3240)
                    val initialLocation = currentLocation ?: defaultLocation

                    map.cameraPosition = CameraPosition.Builder()
                        .target(initialLocation)
                        .zoom(15.0)
                        .build()

                    // Load Baato style
                    val baatoStyleUrl = "https://api.baato.io/api/v1/styles/breeze?key=bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"

                    map.setStyle(Style.Builder().fromUri(baatoStyleUrl)) { style ->
                        android.util.Log.d("BaatoMap", "Style loaded successfully")

                        // Add current location marker if available
                        currentLocation?.let { location ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title("You are here")
                            )
                            android.util.Log.d("BaatoMap", "Current location marker added")
                        }

                        // Draw route if exists
                        if (routePoints.isNotEmpty()) {
                            drawRoute(map, routePoints)
                        }
                    }
                }
            }
        },
        update = { view ->
            view.getMapAsync { map ->
                if (map.style?.isFullyLoaded == true) {
                    // Clear and redraw markers
                    map.clear()

                    // Add current location marker
                    currentLocation?.let { location ->
                        map.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title("You are here")
                        )

                        // Move camera to current location
                        map.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                location,
                                15.0
                            ),
                            1000 // 1 second animation
                        )
                    }

                    // Draw route
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