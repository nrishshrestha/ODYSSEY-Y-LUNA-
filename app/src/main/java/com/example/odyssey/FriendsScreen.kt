package com.example.odyssey

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FriendsScreen(onUserClick: (String) -> Unit = {}){
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            Column(
                verticalArrangement = Arrangement.Center,modifier = Modifier.weight(9f).padding(10.dp)) {
                Text("Friends", style = TextStyle(fontSize = 30.sp))
            }
            Column(horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f).padding(top = 10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                        .clickable{
                        }
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White)) {

        }
    }
}
@Composable
fun AddChat(img: Int, txt: String){
    Card(modifier = Modifier.height(60.dp), shape = RoundedCornerShape(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(img),
                contentDescription = null, modifier = Modifier.size(40.dp))
            Text(txt, style = TextStyle(fontSize = 20.sp),
                modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}

@Preview
@Composable
fun FriendsPreview(){
    FriendsScreen()
}
