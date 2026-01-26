package com.example.odyssey

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle // ADD THIS

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // ADD THIS: Remember the MapView instance
    val mapView = remember { MapView(context) }

    LaunchedEffect(Unit) {
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Where you are",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xD7363636)
        )

        // FIXED AndroidView with proper lifecycle
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .clip(RoundedCornerShape(12.dp)),
            factory = {
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        val styleUrl =
                            "https://api.baato.io/api/v1/styles/breeze?key=bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"

                        map.setStyle(Style.Builder().fromUri(styleUrl)) {
                            // REMOVED: setAllGesturesEnabled(false) - this can cause crashes

                            userLocation?.let { loc ->
                                map.addMarker(
                                    MarkerOptions()
                                        .position(loc)
                                        .title("You are here")
                                )

                                map.cameraPosition =
                                    CameraPosition.Builder()
                                        .target(loc)
                                        .zoom(18.0)
                                        .build()
                            }
                        }
                    }
                }
            },
            update = { view ->
                // Update map when location changes
                view.getMapAsync { map ->
                    if (map.style?.isFullyLoaded == true) {
                        userLocation?.let { loc ->
                            // Clear existing markers
                            map.clear()

                            // Add updated marker
                            map.addMarker(
                                MarkerOptions()
                                    .position(loc)
                                    .title("You are here")
                            )

                            // Move camera smoothly
                            map.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                    loc,
                                    18.0
                                ),
                                1000
                            )
                        }
                    }
                }
            }
        )

        // ADD THIS: Proper lifecycle handling
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

        StatsCard()
        ActionButtons()
        TripsTitle()

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sampleTrips.forEach { trip ->
                TripCard(trip)
            }
        }
    }
}

@Composable
fun StatsCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("12", "Trips Completed")
            StatItem("157 km", "Distance This Month")
            StatItem("54", "Photos Added")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ActionButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedActionButton(
                text = "Start New Trip",
                icon = R.drawable.baseline_directions_walk_24
            )
            OutlinedActionButton(
                text = "Add Note",
                icon = R.drawable.baseline_event_note_24
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedActionButton(
                text = "Add Photo",
                icon = R.drawable.baseline_photo_camera_24
            )
            OutlinedActionButton(
                text = "Chat",
                icon = R.drawable.baseline_chat_24
            )
        }
    }
}

@Composable
fun RowScope.OutlinedActionButton(text: String, icon: Int) {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .weight(1f)
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFBDBDBD))
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.Black, fontSize = 13.sp)
    }
}

@Composable
fun TripsTitle() {
    Text(
        text = "Your Trips",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TripCard(trip: Trip) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(trip.title, fontWeight = FontWeight.Bold)
                Text(trip.details, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

data class Trip(
    val title: String,
    val details: String
)

val sampleTrips = listOf(
    Trip("Everest Base Camp", "Jun 3 • 19 KM • 19 KM"),
    Trip("Annapurna Circuit", "May 21 • 14 KM • 30 KM")
)