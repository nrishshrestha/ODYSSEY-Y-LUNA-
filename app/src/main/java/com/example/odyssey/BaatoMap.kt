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
import com.example.odyssey.utils.drawRoute
import org.maplibre.android.camera.CameraPosition

@Composable
fun BaatoMap(
    routePoints: List<LatLng>,
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
                    // Set initial camera position (Kathmandu, Nepal)
                    val kathmandu = LatLng(27.7172, 85.3240)
                    map.cameraPosition = CameraPosition.Builder()
                        .target(kathmandu)
                        .zoom(12.0)
                        .build()

                    // Use Baato's vector style URL directly
                    val baatoStyleUrl = "https://api.baato.io/api/v1/styles/breeze?key=bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"

                    map.setStyle(Style.Builder().fromUri(baatoStyleUrl)) { style ->
                        // Style loaded successfully
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
                    drawRoute(map, routePoints)
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