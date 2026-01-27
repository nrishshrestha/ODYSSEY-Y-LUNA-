package com.example.odyssey

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.FriendViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.FriendRepoImpl
import com.example.odyssey.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FriendsScreen(
    onUserClick: (String) -> Unit = {},
    onChatClick: (String, String) -> Unit = { _, _ -> },
    onAddFriendClick: () -> Unit = {}
) {
    val friendViewModel: FriendViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FriendViewModel(FriendRepoImpl(UserRepoImpl())) as T
            }
        }
    )

    val friends by friendViewModel.friends.collectAsState()
    val loading by friendViewModel.loading.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            friendViewModel.loadFriends(currentUserId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Friends",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            IconButton(onClick = onAddFriendClick) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = "Add Friend",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
        }

        if (loading && friends.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (friends.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No friends yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(friends) { friend ->
                    FriendCard(
                        friend = friend,
                        onClick = { onUserClick(friend.userId) },
                        onChatClick = { onChatClick(friend.userId, "${friend.firstName} ${friend.lastName}") }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendCard(friend: UserModel, onClick: () -> Unit, onChatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = friend.imageUrl,
                contentDescription = "${friend.firstName}'s profile picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.baseline_person_24),
                error = painterResource(R.drawable.baseline_person_24)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Bio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${friend.firstName} ${friend.lastName}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                )
                if (friend.bio.isNotEmpty()) {
                    Text(
                        text = friend.bio,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.Gray
                        ),
                        maxLines = 1
                    )
                }
            }

            // Message Icon
            IconButton(onClick = onChatClick) {
                Icon(
                    painter = painterResource(R.drawable.baseline_chat_24),
                    contentDescription = "Chat with ${friend.firstName}",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
