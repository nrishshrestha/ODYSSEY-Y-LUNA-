package com.example.odyssey

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odyssey.ViewModel.CreateRouteViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun HomeScreen(
    routeViewModel: CreateRouteViewModel = viewModel(),
    onStartTripClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val userRoutes by routeViewModel.userRoutes.observeAsState(emptyList())

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val mapView = remember { MapView(context) }

    LaunchedEffect(userId) {
        if (userId != null) {
            routeViewModel.getRoutesByUser(userId)
        }
    }

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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Where you are",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xD7363636)
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(12.dp)),
            factory = {
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        val styleUrl =
                            "https://api.baato.io/api/v1/styles/breeze?key=bpk.GUTSn6p8o-LVyDlQOu-S7HLs2gQgI5Y6zkvoAGVlDXMD"

                        map.setStyle(Style.Builder().fromUri(styleUrl)) {
                            userLocation?.let { loc ->
                                map.addMarker(
                                    MarkerOptions()
                                        .position(loc)
                                        .title("You are here")
                                )

                                map.cameraPosition =
                                    CameraPosition.Builder()
                                        .target(loc)
                                        .zoom(15.0)
                                        .build()
                            }
                        }
                    }
                }
            },
            update = { view ->
                view.getMapAsync { map ->
                    if (map.style?.isFullyLoaded == true) {
                        userLocation?.let { loc ->
                            map.clear()
                            map.addMarker(
                                MarkerOptions()
                                    .position(loc)
                                    .title("You are here")
                            )
                            map.animateCamera(
                                org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                    loc,
                                    15.0
                                ),
                                1000
                            )
                        }
                    }
                }
            }
        )

        val totalDurationMs = userRoutes.sumOf { it.duration }
        StatsCard(
            tripsCount = userRoutes.size.toString(),
            totalDuration = formatDurationString(totalDurationMs)
        )

        ActionButtons(onStartTripClick = onStartTripClick, onChatClick = onChatClick)
        
        Spacer(modifier = Modifier.height(20.dp))
    }

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

@Composable
fun StatsCard(tripsCount: String, totalDuration: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(tripsCount, "Trips Completed")
            VerticalDivider(modifier = Modifier.height(30.dp).align(Alignment.CenterVertically))
            StatItem(totalDuration, "Total Duration")
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
fun ActionButtons(onStartTripClick: () -> Unit, onChatClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedActionButton(
            text = "Start New Trip",
            icon = R.drawable.baseline_directions_walk_24,
            onClick = onStartTripClick
        )
        OutlinedActionButton(
            text = "Chat",
            icon = R.drawable.baseline_chat_24,
            onClick = onChatClick
        )
    }
}

@Composable
fun RowScope.OutlinedActionButton(text: String, icon: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
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

private fun formatDurationString(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val totalMinutes = totalSeconds / 60
    val minutes = totalMinutes % 60
    val totalHours = totalMinutes / 60
    val hours = totalHours % 24
    val days = totalHours / 24

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
