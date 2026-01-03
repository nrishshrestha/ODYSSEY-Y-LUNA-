package com.example.odyssey

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odyssey.ViewModel.CreateRouteViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.mapboxsdk.Mapbox
import android.Manifest

@Composable
fun CreateScreen(
    viewModel: CreateRouteViewModel = viewModel()
) {
    val context = LocalContext.current

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startRecording()
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {

        BaatoMap(routePoints = viewModel.routePoints)

        Button(
            onClick = {
                if (viewModel.isRecording) {
                    viewModel.stopRecording()
                } else {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.startRecording()
                    } else {
                        locationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(if (viewModel.isRecording) "Stop" else "Record")
        }
    }
}
