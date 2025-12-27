package com.example.odyssey

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.repository.UserRepoImpl
import com.example.odyssey.ui.theme.ODYSSEYTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ODYSSEYTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    LoginBody()
                }
            }
        }
    }
}


@Composable
fun LoginBody() {
    val keyBoardController = LocalSoftwareKeyboardController.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val context = LocalContext.current
    val activity = context as Activity

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            Text("Sign In",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                color = Color(0xFF3460FB),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
                Text("Or", modifier = Modifier.padding(horizontal = 15.dp))

                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { data ->
                    email = data
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("example@gmail.com")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFDFDFD),
                    unfocusedContainerColor = Color(0xFFDFE0EB),
                    focusedIndicatorColor = Color(0xFF596096),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { data ->
                    password = data
                },
                trailingIcon = {
                    IconButton (onClick = {
                        visibility = !visibility
                    }) {
                        Icon(
                            painter = if (visibility)
                                painterResource(R.drawable.baseline_visibility_off_24)
                            else
                                painterResource(R.drawable.baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("*********")
                },

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F6FC),
                    unfocusedContainerColor = Color(0xFFDDDEE9),
                    focusedIndicatorColor = Color(0xFF596096),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Text(
                "Forgot Password?",
                style = TextStyle(
                    color = Color(0xFF3460FB),
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 10.dp).clickable {
                        val intent = Intent(context, ForgetPasswordActivity::class.java)
                        context.startActivity(intent)

                    },
            )
            Spacer(modifier = Modifier.height(25.dp))

            Button(onClick = {
                keyBoardController?.hide()
                if (email.isNotBlank() && password.isNotBlank()) {
                    userViewModel.login(email, password) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, DashboardActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        } else {
                            Toast.makeText(context, message ?: "Invalid details", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .height(60.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 15.dp
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Log In")
            }

            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(style = SpanStyle(color = Color(0xFF3460FB), fontWeight = FontWeight.Bold)) {
                        append("Sign Up")
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 10.dp).clickable {
                        val intent = Intent(context, SignUpActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SocialMediaCard(
    modifier: Modifier = Modifier, image: Int, label: String
) {
    Card(modifier) {
        Row(modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(label)
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    LoginBody()
}