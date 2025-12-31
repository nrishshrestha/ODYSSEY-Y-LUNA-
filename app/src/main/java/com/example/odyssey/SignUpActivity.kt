package com.example.odyssey

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.toSize
import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepoImpl
import com.example.odyssey.ui.theme.ODYSSEYTheme
import java.util.Calendar

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ODYSSEYTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    SignUpBody()
                }
            }
        }
    }
}

@Composable
fun SignUpBody(){


    var firstName by remember {mutableStateOf("")}
    var lastName by remember {mutableStateOf("")}
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var terms by remember { mutableStateOf(false) }
    var selectDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("Select Gender") }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    val context = LocalContext.current
    val activity = context as Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    val options = listOf("Male", "Female")
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datepicker = DatePickerDialog(
        context,
        { _, mYear, mMonth, mDay ->
            selectDate = "$mDay/${mMonth + 1}/$mYear"
        }, year, month, day
    )


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            Text("Sign Up",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                color = Color(0xFF3460FB),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = {data ->
                    firstName = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("First Name")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
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
                value = lastName,
                onValueChange = {data ->
                    lastName = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Last Name")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
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
                value = email,
                onValueChange = { data ->
                    email = data
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
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
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                enabled = false,
                value = selectDate,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .clickable {
                        datepicker.show()
                    },
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text("Select Date")
                },
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    focusedContainerColor = Color(0xFFDDDEE9),
                    unfocusedContainerColor = Color(0xFFDDDEE9),
                    disabledContainerColor = Color(0xFFDDDEE9),
                    focusedIndicatorColor = Color(0xFF596096),
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                OutlinedTextField(
                    value = selectedOptionText,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        }
                        .clickable {
                            expanded = true
                        },
                    shape = RoundedCornerShape(15.dp),
                    placeholder = {
                        Text("Select Gender")
                    },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            painter = if (expanded)
                                painterResource(R.drawable.outline_keyboard_arrow_up_24)
                            else
                                painterResource(R.drawable.baseline_keyboard_arrow_down_24),
                            contentDescription = null
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFDDDEE9),
                        unfocusedContainerColor = Color(0xFFDDDEE9),
                        disabledContainerColor = Color(0xFFDDDEE9),
                        focusedIndicatorColor = Color(0xFF596096),
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) {textFieldSize.width.toDp()})
                ) { options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            selectedOptionText = option
                            expanded = false
                        }
                    )
                }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = terms,
                    onCheckedChange = {
                        terms = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF596096),
                        checkmarkColor = Color.White
                    )
                )
                Text("I agree to the terms and conditions")
            }
            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    if (!terms) {
                        Toast.makeText(
                            context,
                            "please agree to terms &  conditions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        userViewModel.register(email.trim(),password.trim()) {
                                success, message, userId ->
                            if(success) {
                                var model = UserModel(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    gender = selectedOptionText,
                                    email = email,
                                    dob = selectDate
                                )
                                userViewModel.addUserToDatabase(
                                    userId,model
                                ) {
                                        success,message ->
                                    if(success) {
                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }else {
                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }else{
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }, modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White, containerColor = Color.Blue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sign Up", style = TextStyle(fontSize = 20.sp))
            }

            Text(
                text = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = Color(0xFF3460FB), fontWeight = FontWeight.Bold)) {
                        append("Login")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .clickable {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        activity.finish()
                    },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun SignUpPreview(){
    SignUpBody()
}