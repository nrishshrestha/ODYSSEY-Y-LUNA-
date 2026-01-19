package com.example.odyssey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odyssey.ViewModel.NotificationViewModel
import com.example.odyssey.ViewModel.UserViewModel
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

    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val userViewModel = remember { UserViewModel(UserRepoImpl(), FriendRepoImpl(UserRepoImpl())) }
    val notificationViewModel = remember { NotificationViewModel() }
    
    val unreadCount by notificationViewModel.unreadCount.observeAsState(0)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            notificationViewModel.fetchNotifications(currentUserId)
        }
    }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Trips", R.drawable.baseline_map_24),
        NavItem("Create", R.drawable.baseline_add_24),
        NavItem("Friends", R.drawable.baseline_people_24),
        NavItem("Profile", R.drawable.baseline_person_24),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (selectedItem != 0 || showNotifications || showSearch) {
                        IconButton(onClick = { 
                            if (showNotifications) showNotifications = false 
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
                        },
                        onSearchClick = {
                            showSearch = true
                            showNotifications = false
                        },
                        unreadCount = unreadCount
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
                        },
                        selected = !showNotifications && !showSearch && selectedItem == index
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
                SearchScreen(showTopBar = false)
            } else {
                when (selectedItem) {
                    0 -> HomeScreen()
                    1 -> Text(text = "Trips")
                    2 -> CreateScreen()
                    3 -> FriendsScreen()
                    4 -> UserProfileBody(showTopBar = false)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(onNotificationClick: () -> Unit, onSearchClick: () -> Unit, unreadCount: Int) {
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
            IconButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    contentDescription = "Search"
                )
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
        }
    }
}
