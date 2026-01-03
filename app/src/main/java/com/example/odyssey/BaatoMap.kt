package com.example.odyssey

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.odyssey.utils.drawRoute
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

@Composable
fun BaatoMap(
    routePoints: List<LatLng>
) {
    val context = LocalContext.current

    val mapboxMapState = remember { mutableStateOf<MapboxMap?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            MapView(context).apply {
                onCreate(null)

                getMapAsync { mapboxMap ->
                    mapboxMapState.value = mapboxMap

                    mapboxMap.setStyle(
                        Style.Builder().fromUri(
                            "https://api.baato.io/api/v1/styles/baato-dark?key=YOUR_BAATO_API_KEY"
                        )
                    )
                }
            }
        },
        update = {
            mapboxMapState.value?.let { mapboxMap ->
                drawRoute(mapboxMap, routePoints)
            }
        }
    )
}
