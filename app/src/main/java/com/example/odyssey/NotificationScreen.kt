package com.example.odyssey

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.NotificationViewModel
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.NotificationModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.FriendRepoImpl
import com.example.odyssey.repository.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    showTopBar: Boolean = true,
    notificationViewModel: NotificationViewModel = remember { NotificationViewModel() },
    userViewModel: UserViewModel = remember { UserViewModel(UserRepoImpl(), FriendRepoImpl(UserRepoImpl())) }
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val notifications by notificationViewModel.notifications.observeAsState(emptyList())

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            notificationViewModel.fetchNotifications(currentUserId)
        }
    }

    val content = @Composable { paddingValues: PaddingValues ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification, notificationViewModel, userViewModel)
                }
            }
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                    actions = {
                        if (notifications.any { !it.isRead }) {
                            IconButton(onClick = {
                                notificationViewModel.markAllAsRead(currentUserId)
                            }) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                            }
                        }
                    }
                )
            }
        ) { padding -> content(padding) }
    } else {
        Column {
            if (notifications.any { !it.isRead }) {
                TextButton(
                    onClick = { notificationViewModel.markAllAsRead(currentUserId) },
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark all as read", fontSize = 12.sp)
                }
            }
            content(PaddingValues(0.dp))
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationModel, 
    notificationViewModel: NotificationViewModel,
    userViewModel: UserViewModel
) {
    var senderData by remember { mutableStateOf<UserModel?>(null) }
    val context = LocalContext.current
    val requestId = notification.metadata["requestId"]
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(notification.fromUserId) {
        UserRepoImpl().getUserByID(notification.fromUserId) { success, _, user ->
            if (success) senderData = user
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead) {
                    notificationViewModel.markNotificationAsRead(notification.id)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color(0xFFF8F9FA) else Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = senderData?.imageUrl?.ifEmpty { "https://via.placeholder.com/150" } ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = senderData?.firstName?.let { "$it ${senderData?.lastName}" } ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = notification.content,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3460FB))
                    )
                }
            }

            if (notification.type == "FOLLOW" && requestId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            userViewModel.declineRequest(requestId) { success, msg ->
                                if (success) {
                                    notificationViewModel.updateNotification(
                                        notification.id,
                                        mapOf(
                                            "content" to "Follow request declined",
                                            "type" to "FOLLOW_DECLINED",
                                            "isRead" to true
                                        )
                                    )
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Decline", fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            userViewModel.acceptRequest(requestId) { success, msg ->
                                if (success) {
                                    // Update current notification
                                    notificationViewModel.updateNotification(
                                        notification.id,
                                        mapOf(
                                            "content" to "Follow request accepted",
                                            "type" to "FOLLOW_ACCEPTED",
                                            "isRead" to true
                                        )
                                    )
                                    
                                    // Send notification to the sender
                                    val replyNotification = NotificationModel(
                                        fromUserId = currentUserId,
                                        toUserId = notification.fromUserId,
                                        type = "FOLLOW_ACCEPTED_REPLY",
                                        content = "accepted your follow request",
                                        timestamp = System.currentTimeMillis(),
                                        isRead = false
                                    )
                                    notificationViewModel.sendNotification(replyNotification)
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accept", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
