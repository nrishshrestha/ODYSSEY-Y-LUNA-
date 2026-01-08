package com.example.odyssey

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odyssey.ViewModel.CreateRouteViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreateScreen(
    viewModel: CreateRouteViewModel = viewModel()
) {
    val context = LocalContext.current

    // Location permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request permissions and get current location
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            // Get current location when permissions are granted
            viewModel.getCurrentLocation(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map with current location
        BaatoMap(
            routePoints = viewModel.routePoints,
            currentLocation = viewModel.currentLocation.value,
            modifier = Modifier.fillMaxSize()
        )

        // Record button at bottom
        Button(
            onClick = {
                if (permissionsState.allPermissionsGranted) {
                    if (viewModel.isRecording.value) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording(context)
                    }
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (viewModel.isRecording.value) "Stop Recording" else "Record"
            )
        }

        // Permission message if not granted
        if (!permissionsState.allPermissionsGranted) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Location permission is required to record your journey",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}