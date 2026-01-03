package com.example.odyssey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odyssey.ViewModel.CreateRouteViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.mapboxsdk.Mapbox

@Composable
fun CreateScreen(
    viewModel: CreateRouteViewModel = viewModel()
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // MAP
        BaatoMap(
            routePoints = viewModel.routePoints
        )

        // BUTTON
        RecordButton(
            isRecording = viewModel.isRecording,
            onClick = {
                if (viewModel.isRecording) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording()
                }
            }
        )
    }
}
