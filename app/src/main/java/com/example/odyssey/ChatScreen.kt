package com.example.odyssey

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.odyssey.ViewModel.ChatViewModel
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    toUserId: String,
    toUserName: String,
    chatViewModel: ChatViewModel
) {
    val messages by chatViewModel.messages.observeAsState(emptyList())
    var textState by remember(toUserId) { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserName = FirebaseAuth.getInstance().currentUser?.email ?: "User"
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(toUserId) {
        chatViewModel.initChat(currentUserId, currentUserName, toUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { chatViewModel.sendMedia(toUserId, it, ChatMessageType.IMAGE) }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { chatViewModel.sendMedia(toUserId, it, ChatMessageType.VIDEO) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color.LightGray
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(toUserName.take(1).uppercase(), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(toUserName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Active now", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Info, null, tint = Color(0xFF3460FB)) }
                }
            )
        },
        bottomBar = {
            ChatInput(
                text = textState,
                onTextChange = { textState = it },
                onSend = {
                    if (textState.isNotBlank()) {
                        chatViewModel.sendMessage(toUserId, textState)
                        textState = ""
                    }
                },
                onImagePick = { imagePicker.launch("image/*") },
                onVideoPick = { videoPicker.launch("video/*") }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderId == "me" || message.senderId == currentUserId
                ChatBubble(message, isMe)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) Color(0xFF0084FF) else Color(0xFFF0F0F0),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                when (message.type) {
                    ChatMessageType.TEXT -> {
                        Text(
                            text = message.message,
                            color = if (isMe) Color.White else Color.Black,
                            fontSize = 15.sp
                        )
                    }
                    ChatMessageType.IMAGE -> {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Image message",
                            modifier = Modifier
                                .sizeIn(maxWidth = 200.dp, maxHeight = 300.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    ChatMessageType.VIDEO -> {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = message.thumbnailUrl ?: message.mediaUrl,
                                contentDescription = "Video preview",
                                modifier = Modifier
                                    .sizeIn(maxWidth = 200.dp, maxHeight = 300.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                    else -> {
                        Text("File attachment", color = if (isMe) Color.White else Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onImagePick: () -> Unit,
    onVideoPick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = Color.White,
        modifier = Modifier.navigationBarsPadding().imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video icon shifted to the left
            IconButton(onClick = onVideoPick) { 
                Icon(Icons.Default.Videocam, null, tint = Color(0xFF0084FF), modifier = Modifier.size(24.dp)) 
            }
            
            // Image icon
            IconButton(onClick = onImagePick) { 
                Icon(Icons.Default.Image, null, tint = Color(0xFF0084FF), modifier = Modifier.size(24.dp)) 
            }
            
            // Compact message field using BasicTextField to save space
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0F2F5)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color.Black),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text("Aa", color = Color.Gray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }
            
            // Send button always visible on the right
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, 
                    null, 
                    tint = if (text.isNotBlank()) Color(0xFF0084FF) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
