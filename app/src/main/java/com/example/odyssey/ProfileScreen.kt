package com.example.odyssey

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.ProfileViewModel
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val user by profileViewModel.user.collectAsState()

    if (user == null) {
        Text("Loading profile...")
        return
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            selectedImageUri = uri
        }

    ProfileScreenBody(
        user = user!!,
        selectedImageUri = selectedImageUri,
        onPickImage = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}
@Composable
fun ProfileScreenBody(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    user: UserModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{

            Row(modifier = Modifier.fillMaxWidth()
                ,horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Profile", style = TextStyle(fontSize = 30.sp),
                    modifier = Modifier.padding(start = 10.dp))
                Text("Logout", style = TextStyle(Color.Red,fontSize = 30.sp),
                    modifier = Modifier.padding(end = 10.dp).clickable{

                    })
            }

            //Profile picture and name
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onPickImage()
                        }
                ) {
                    val imageModel = selectedImageUri
                        ?: user.imageUrl.ifEmpty { R.drawable.profilepictureplaceholder }

                    AsyncImage(
                        model = imageModel, // Use the dynamically chosen model
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize() // Use fillMaxSize to fill the Box
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        // Provide a fallback in case the URL fails to load
                        error = painterResource(id = R.drawable.profilepictureplaceholder)
                    )
                }
                Text("${user.firstName} ${user.lastName}")
                Text(user.email)
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//fun ProfilePreview() {
//    ProfileScreenBody(
//        selectedImageUri = null, onPickImage = {},
//        user = UserModel,
//    )
//}
