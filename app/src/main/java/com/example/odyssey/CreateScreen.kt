package com.example.odyssey

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    // Permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAndSavePhoto(context, uri)
        }
    }

    // Request location permissions
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        } else {
            viewModel.getCurrentLocation(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map
        BaatoMap(
            routePoints = viewModel.getLatLngPoints(),
            currentLocation = viewModel.currentLocation.value,
            modifier = Modifier.fillMaxSize()
        )

        // === TIMER DISPLAY AT TOP ===
        if (viewModel.isRecording.value) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Recording",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = viewModel.timerDisplay.value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Recording controls (bottom)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Note and Photo buttons (only show while recording)
            if (viewModel.isRecording.value) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Note Button
                    FloatingActionButton(
                        onClick = { viewModel.openNoteDialog() }
                    ) {
                        Icon(Icons.Default.Note, contentDescription = "Add Note")
                    }

                    // Add Photo Button - FIXED VERSION
                    FloatingActionButton(
                        onClick = {
                            if (!viewModel.isUploadingPhoto.value) {
                                photoPickerLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier.alpha(if (viewModel.isUploadingPhoto.value) 0.5f else 1f)
                    ) {
                        if (viewModel.isUploadingPhoto.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo")
                        }
                    }
                }

                // Upload progress
                if (viewModel.isUploadingPhoto.value) {
                    Text(
                        text = "Uploading... ${viewModel.uploadProgress.value}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Record/Stop Button
            Button(
                onClick = {
                    if (locationPermissions.allPermissionsGranted) {
                        if (viewModel.isRecording.value) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording(context)
                        }
                    } else {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                },
                colors = if (viewModel.isRecording.value) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(
                    text = if (viewModel.isRecording.value) "Stop Recording" else "Record"
                )
            }
        }

        // Permission message
        if (!locationPermissions.allPermissionsGranted) {
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

    // === DIALOGS ===

    // Note Dialog
    if (viewModel.showNoteDialog.value) {
        AlertDialog(
            onDismissRequest = { viewModel.closeNoteDialog() },
            title = { Text("Add Note") },
            text = {
                OutlinedTextField(
                    value = viewModel.pendingNote.value,
                    onValueChange = { viewModel.updatePendingNote(it) },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveNote() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeNoteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save Route Dialog
    if (viewModel.showSaveRouteDialog.value) {
        AlertDialog(
            onDismissRequest = { viewModel.closeSaveRouteDialog() },
            title = { Text("Save Route") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Duration: ${viewModel.timerDisplay.value}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = viewModel.routeTitle.value,
                        onValueChange = { viewModel.updateRouteTitle(it) },
                        label = { Text("Route Title*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = viewModel.routeDescription.value,
                        onValueChange = { viewModel.updateRouteDescription(it) },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Text(
                        text = "Points recorded: ${viewModel.routePoints.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveRouteToFirebase(
                            onSuccess = {
                                Toast.makeText(context, "Route saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { exception ->
                                Toast.makeText(context, "Failed to save: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeSaveRouteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}