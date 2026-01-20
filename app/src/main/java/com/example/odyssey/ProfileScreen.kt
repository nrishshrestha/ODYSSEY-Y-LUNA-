package com.example.odyssey

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.ProfileViewModel
import com.example.odyssey.model.UserModel

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val user by profileViewModel.user.collectAsState()
    val uploading by profileViewModel.uploading.collectAsState()
    val uploadError by profileViewModel.uploadError.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var previousUploadingState by remember { mutableStateOf(false) }

    // Log when user data changes
    LaunchedEffect(user?.imageUrl) {
        android.util.Log.d("ProfileScreen", "User imageUrl updated: ${user?.imageUrl}")
    }

    // Show toast when upload completes
    LaunchedEffect(uploading, uploadError, user?.imageUrl) {
        // Check if upload just finished
        if (previousUploadingState && !uploading) {
            if (uploadError != null) {
                // Upload failed
                Toast.makeText(
                    context,
                    "Failed to upload profile picture: $uploadError",
                    Toast.LENGTH_LONG
                ).show()
            } else if (user?.imageUrl?.isNotEmpty() == true) {
                // Upload succeeded
                Toast.makeText(
                    context,
                    "Profile picture uploaded successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                selectedImageUri = null
            }
        }
        previousUploadingState = uploading
    }

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            profileViewModel.uploadProfileImage(context, uri)
        }
    }

    ProfileScreenBody(
        user = user,
        selectedImageUri = selectedImageUri,
        uploading = uploading,
        uploadError = uploadError,
        onPickImage = {
            imagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}

@Composable
fun ProfileScreenBody(
    user: UserModel?,
    selectedImageUri: Uri?,
    uploading: Boolean,
    uploadError: String?,
    onPickImage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Profile",
                    style = TextStyle(fontSize = 30.sp),
                    modifier = Modifier.padding(start = 10.dp)
                )
                Text(
                    "Logout",
                    style = TextStyle(Color.Red, fontSize = 30.sp),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .clickable { }
                )
            }

            // Profile picture and name
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPickImage, enabled = !uploading)
                ) {
                    // Priority: 1. selectedImageUri (just picked), 2. user.imageUrl (from Firebase), 3. placeholder
                    val imageModel = when {
                        selectedImageUri != null -> selectedImageUri
                        !user?.imageUrl.isNullOrEmpty() -> user?.imageUrl
                        else -> R.drawable.profilepictureplaceholder
                    }

                    // Add key to force recomposition when imageUrl changes
                    androidx.compose.runtime.key(user?.imageUrl) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.profilepictureplaceholder),
                            placeholder = painterResource(id = R.drawable.profilepictureplaceholder)
                        )
                    }

                    // Show loading overlay when uploading
                    if (uploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show upload status text
                if (uploading) {
                    Text(
                        text = "Uploading...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (user != null) {
                    Text("${user.firstName} ${user.lastName}")
                    Text(user.email)
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview(){
    ProfileScreenBody( null, null, false, null, {})
}
