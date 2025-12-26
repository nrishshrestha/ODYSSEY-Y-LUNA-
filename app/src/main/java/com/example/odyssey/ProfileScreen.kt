package com.example.odyssey

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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


@Composable
fun ProfileScreenBody() {
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
                Image(
                    painter = painterResource(R.drawable.profilepictureplaceholder),
                    contentDescription = null,
                    modifier = Modifier.height(120.dp)
                        .width(120.dp)
                        .clip(CircleShape)
                        .clickable{

                        },
                    contentScale = ContentScale.Crop
                )

            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProfilePreview() {
    ProfileScreenBody()
}
