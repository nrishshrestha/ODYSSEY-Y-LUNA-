package com.example.odyssey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepoImpl

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SearchScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var searchQuery by remember { mutableStateOf("") }
    val allUsers by userViewModel.allUsers.observeAsState(emptyList())

    // Fetch users when the screen opens
    LaunchedEffect(Unit) {
        userViewModel.getAllUser()
    }

    // Filter logic: Search by first or last name and take top 5
    val filteredUsers = remember(searchQuery, allUsers) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            allUsers.filterNotNull()
                .filter { user ->
                    user.firstName.contains(searchQuery, ignoreCase = true) ||
                    user.lastName.contains(searchQuery, ignoreCase = true)
                }
                .take(5)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Users", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Search by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isNotEmpty() && filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserSearchItem(user)
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(user: UserModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to profile */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.imageUrl.ifEmpty { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (user.bio.isNotEmpty()) {
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchPreview(){
    SearchScreen()
}