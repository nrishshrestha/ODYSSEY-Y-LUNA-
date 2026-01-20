package com.example.odyssey

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.FriendRepoImpl
import com.example.odyssey.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth

class UserProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val targetUserId = intent.getStringExtra("userId")
        setContent {
            UserProfileBody(targetUserId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileBody(targetUserId: String? = null, showTopBar: Boolean = true) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    
    // Determine if we are looking at our own profile or someone else's
    val isOwnProfile = targetUserId == null || targetUserId == currentUserId
    val effectiveUserId = targetUserId ?: currentUserId

    val userViewModel = remember { UserViewModel(UserRepoImpl(), FriendRepoImpl(UserRepoImpl())) }
    
    val userData by if (isOwnProfile) userViewModel.user.observeAsState() else userViewModel.otherUser.observeAsState()
    val isFollowing by userViewModel.isFollowing.observeAsState(false)
    val followersCount by userViewModel.followersCount.observeAsState(0)
    val followingCount by userViewModel.followingCount.observeAsState(0)

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(effectiveUserId) {
        if (effectiveUserId.isNotEmpty()) {
            if (isOwnProfile) {
                userViewModel.getUserByID(effectiveUserId)
            } else {
                userViewModel.getOtherUserByID(effectiveUserId)
                userViewModel.checkFollowingStatus(currentUserId, effectiveUserId)
            }
        }
    }

    LaunchedEffect(userData) {
        userData?.let {
            name = "${it.firstName} ${it.lastName}".trim()
            bio = it.bio
            profileImageUrl = it.imageUrl
        }
    }

    val content = @Composable { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ProfileHeader(profileImageUrl, followersCount, followingCount)

            Spacer(modifier = Modifier.height(16.dp))

            ProfileBio(name, bio)

            Spacer(modifier = Modifier.height(16.dp))

            ProfileActions(
                isOwnProfile = isOwnProfile,
                isFollowing = isFollowing,
                onEditClick = { showEditDialog = true },
                onFollowClick = {
                    userViewModel.followUser(currentUserId, effectiveUserId) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabSection()
            PostGrid()
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(userData?.email ?: "Profile", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    } else {
        content(PaddingValues(0.dp))
    }

    if (showEditDialog && userData != null && isOwnProfile) {
        EditProfileDialog(
            userModel = userData!!,
            userViewModel = userViewModel,
            onSave = { updatedModel ->
                userViewModel.editProfile(currentUserId, updatedModel) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun ProfileHeader(imageUrl: String, followers: Int, following: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl.ifEmpty { "https://via.placeholder.com/150" },
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.weight(3f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserStats("Trips", "0")
            UserStats("Followers", followers.toString())
            UserStats("Following", following.toString())
        }
    }
}

@Composable
fun UserStats(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileBio(name: String, bio: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(name, fontWeight = FontWeight.Bold)
        Text(bio.ifEmpty { "No bio yet" }, fontSize = 14.sp)
    }
}

@Composable
fun ProfileActions(
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isOwnProfile) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFEFEF),
                    contentColor = Color.Black
                )
            ) {
                Text("Edit Profile")
            }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFEFEF),
                    contentColor = Color.Black
                )
            ) {
                Text("Share Profile")
            }
        } else {
            Button(
                onClick = if (isFollowing) ({}) else onFollowClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Gray else Color(0xFF3460FB),
                    contentColor = Color.White
                )
            ) {
                Text(if (isFollowing) "Followed" else "Follow")
            }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFEFEF),
                    contentColor = Color.Black
                )
            ) {
                Text("Message")
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    userModel: UserModel,
    userViewModel: UserViewModel,
    onSave: (UserModel) -> Unit,
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(userModel.firstName) }
    var lastName by remember { mutableStateOf(userModel.lastName) }
    var bio by remember { mutableStateOf(userModel.bio) }
    var imageUrl by remember { mutableStateOf(userModel.imageUrl) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            userViewModel.uploadImage(context, it)
            // Note: The UI will update automatically via the getUserByID listener 
            // once Firebase updates, but we can set it locally for instant feedback if needed.
            imageUrl = it.toString() 
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = imageUrl.ifEmpty { "https://via.placeholder.com/150" },
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF3460FB), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF3460FB))
                ) {
                    Text("Change Profile Picture")
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedModel = userModel.copy(
                    firstName = firstName,
                    lastName = lastName,
                    bio = bio,
                    imageUrl = imageUrl
                )
                onSave(updatedModel)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TabSection() {
    Column {
        HorizontalDivider(thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.GridOn, null)
        }
    }
}

@Composable
fun PostGrid() {
    val dummyImages = List(0) {
        "https://picsum.photos/seed/${it + 40}/300/300"
    }

    if (dummyImages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No posts yet", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(dummyImages.size) {
                AsyncImage(
                    model = dummyImages[it],
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    UserProfileBody()
}
