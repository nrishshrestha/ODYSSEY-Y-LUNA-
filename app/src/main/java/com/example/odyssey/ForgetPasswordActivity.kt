package com.example.odyssey

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.repository.UserRepoImpl

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgetPasswordBody()
        }
    }
}

@Composable
fun ForgetPasswordBody() {
    var emailField by remember { mutableStateOf("") }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val context= LocalContext.current
    val activity=context as Activity
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Forgot Password", modifier = Modifier.padding(20.dp),
                    color = Color.Blue, style = TextStyle(fontSize = 30.sp))
                OutlinedTextField(
                    value = emailField,
                    onValueChange = { data ->
                        emailField = data
                    },
                    placeholder = { Text("Enter email") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F6FC),
                        unfocusedContainerColor = Color(0xFFDDDEE9),
                        focusedIndicatorColor = Color(0xFF596096),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                )
                Button(
                    onClick = {
                        if (emailField.isNotBlank()) {
                            userViewModel.forgetPassword(emailField) { success, message ->
                                if (success) {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    activity.finish()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show()
                        }
                    }, modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 20.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("submitButton"),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.White, containerColor = Color.Blue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit", style = TextStyle(fontSize = 20.sp))
                }
            }
        }
    }
}
@Preview
@Composable
fun ForgetPasswordPreview() {
    ForgetPasswordBody()
}