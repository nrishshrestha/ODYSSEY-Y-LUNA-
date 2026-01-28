package com.example.odyssey

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.odyssey.ViewModel.ChatViewModel
import com.example.odyssey.ViewModel.NotificationViewModel
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.repository.ChatRepoImpl
import com.example.odyssey.repository.FriendRepoImpl
import com.example.odyssey.repository.UserRepoImpl
import com.example.odyssey.ui.theme.ODYSSEYTheme
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ODYSSEYTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    DashboardBody()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody() {
    val context = LocalContext.current
    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var selectedChatUserId by remember { mutableStateOf<String?>(null) }
    var selectedChatUserName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val userViewModel = remember { UserViewModel(UserRepoImpl(), FriendRepoImpl(UserRepoImpl())) }
    val notificationViewModel = remember { NotificationViewModel() }

    val chatViewModel: ChatViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(ChatRepoImpl(context.applicationContext as Application)) as T
            }
        }
    )

    val unreadCount by notificationViewModel.unreadCount.observeAsState(0)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            notificationViewModel.fetchNotifications(currentUserId)
        }
    }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Create", R.drawable.baseline_add_24),
        NavItem("Friends", R.drawable.baseline_people_24),
        NavItem("Profile", R.drawable.baseline_person_24),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (selectedItem != 0 || showNotifications || showSearch || selectedChatUserId != null || selectedUserId != null) {
                        IconButton(onClick = {
                            if (selectedChatUserId != null) selectedChatUserId = null
                            else if (selectedUserId != null) selectedUserId = null
                            else if (showNotifications) showNotifications = false
                            else if (showSearch) showSearch = false
                            else selectedItem = 0
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                title = {
                    Header(
                        onNotificationClick = {
                            showNotifications = true
                            showSearch = false
                            selectedChatUserId = null
                            selectedUserId = null
                        },
                        onSearchClick = {
                            showSearch = true
                            showNotifications = false
                            selectedChatUserId = null
                            selectedUserId = null
                        },
                        onLogoutClick = {
                            // Logout functionality
                            FirebaseAuth.getInstance().signOut()
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        onDeleteAccountClick = {
                            showDeleteDialog = true
                        },
                        unreadCount = unreadCount,
                        hideSearch = showSearch || (selectedItem == 3 && !showNotifications && selectedChatUserId == null && selectedUserId == null)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(item.label) },
                        onClick = {
                            selectedItem = index
                            showNotifications = false
                            showSearch = false
                            selectedChatUserId = null
                            selectedUserId = null
                        },
                        selected = !showNotifications && !showSearch && selectedChatUserId == null && selectedUserId == null && selectedItem == index
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showNotifications) {
                NotificationScreen(
                    showTopBar = false,
                    notificationViewModel = notificationViewModel,
                    userViewModel = userViewModel
                )
            } else if (showSearch) {
                SearchScreen(
                    showTopBar = false,
                    onUserClick = { userId ->
                        selectedUserId = userId
                        showSearch = false
                    }
                )
            } else if (selectedChatUserId != null) {
                ChatScreen(
                    toUserId = selectedChatUserId!!,
                    toUserName = selectedChatUserName,
                    chatViewModel = chatViewModel
                )
            } else if (selectedUserId != null) {
                UserProfileBody(
                    targetUserId = selectedUserId,
                    showTopBar = false,
                    onMessageClick = { userId, userName ->
                        selectedChatUserId = userId
                        selectedChatUserName = userName
                    }
                )
            } else {
                when (selectedItem) {
                    0 -> HomeScreen(
                        onStartTripClick = { selectedItem = 1 },
                        onChatClick = { selectedItem = 2 }
                    )
                    1 -> CreateScreen()
                    2 -> FriendsScreen(
                        onUserClick = { userId ->
                            selectedUserId = userId
                        },
                        onChatClick = { userId, userName ->
                            selectedChatUserId = userId
                            selectedChatUserName = userName
                        },
                        onAddFriendClick = {
                            showSearch = true
                        }
                    )
                    3 -> UserProfileBody(
                        showTopBar = false,
                        onMessageClick = { userId, userName ->
                            selectedChatUserId = userId
                            selectedChatUserName = userName
                        }
                    )
                }
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete your account?",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone. All your data including:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Profile information", fontSize = 13.sp, color = Color.Gray)
                    Text("• Friends and connections", fontSize = 13.sp, color = Color.Gray)
                    Text("• Routes and trips", fontSize = 13.sp, color = Color.Gray)
                    Text("• Messages and notifications", fontSize = 13.sp, color = Color.Gray)
                    Text("• All media uploads", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "will be permanently deleted.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        if (currentUserId.isNotEmpty()) {
                            userViewModel.deleteAccount(currentUserId) { success, message ->
                                if (success) {
                                    // Sign out from Firebase Auth
                                    FirebaseAuth.getInstance().currentUser?.delete()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                FirebaseAuth.getInstance().signOut()
                                                Toast.makeText(
                                                    context,
                                                    "Account deleted successfully",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                // Navigate to login
                                                val intent = Intent(context, LoginActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                context.startActivity(intent)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to delete authentication: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to delete account: $message",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    unreadCount: Int,
    hideSearch: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("ODYSSEY", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Let's Travel", fontSize = 14.sp, color = Color.Gray)
        }
        Row {
            if (!hideSearch) {
                IconButton(onClick = onSearchClick,
                    modifier = Modifier.testTag("search_icon")) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_search_24),
                        contentDescription = "Search"
                    )
                }
            }
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_notifications_24),
                        contentDescription = "Notifications"
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_more_vert_24),
                        contentDescription = "More"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Log Out Button
                    DropdownMenuItem(
                        text = {
                            Button(
                                onClick = {
                                    showMenu = false
                                    onLogoutClick()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB21F1F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Log Out",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Delete Account Button
                    DropdownMenuItem(
                        text = {
                            Button(
                                onClick = {
                                    showMenu = false
                                    onDeleteAccountClick()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Delete Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }
    }
}