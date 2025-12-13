package com.example.odyssey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { StatsCard() }
        item { ActionButtons() }

    }
}
@Composable
fun StatsCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("12", "Trips Completed")
            StatItem("157 km", "Distance This Month")
            StatItem("54", "Photos Added")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ActionButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedActionButton(
                text = "Start New Trip",
                icon = R.drawable.baseline_directions_walk_24
            )
            OutlinedActionButton(
                text = "Add Note",
                icon = R.drawable.baseline_event_note_24
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedActionButton(
                text = "Add Photo",
                icon = R.drawable.baseline_photo_camera_24
            )
            OutlinedActionButton(
                text = "Chat",
                icon = R.drawable.baseline_chat_24
            )
        }
    }
}

@Composable
fun RowScope.OutlinedActionButton(text: String, icon: Int) {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .weight(1f)
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFBDBDBD))
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.Black)
    }
}




@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}