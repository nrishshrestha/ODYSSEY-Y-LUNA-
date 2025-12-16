package com.example.odyssey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody()

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody() {

    data class NavItem(val label: String, val icon: Int)

    var selectedItem by remember { mutableStateOf(0) }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Trips", R.drawable.baseline_map_24),
        NavItem("Create",R.drawable.baseline_add_24),
        NavItem("Friends", R.drawable.baseline_people_24),
        NavItem("Profile", R.drawable.baseline_person_24),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Odyssey")
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = null
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
                        label = {Text(item.label)},
                        onClick = {
                            selectedItem = index
                        },
                        selected = selectedItem == index
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
            when(selectedItem) {
                0 -> Text(text = "Home")
                1 -> Text(text = "Trips")
                2 -> Text(text = "Create")
                3 -> Text(text = "Friends")
                4 -> ProfileScreenBody()
            }
        }

    }
}


@Preview
@Composable
fun DashboardPreview() {
    DashboardBody()
}